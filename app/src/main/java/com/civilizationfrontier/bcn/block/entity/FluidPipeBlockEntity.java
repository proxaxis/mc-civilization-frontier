package com.civilizationfrontier.bcn.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.civilizationfrontier.bcn.block.BcnPipeBlock;
import com.civilizationfrontier.bcn.block.BcnPipeBlock.Conn;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FluidPipeBlockEntity extends BlockEntity {

	public static final float SPEED = 0.1F;
	private static final int MAX_TRANSIT = 4;
	private static final long BLOB_MAX = FluidConstants.BUCKET; // 1バケツ=81000
	private static final long EXTRACT_AMOUNT = FluidConstants.BUCKET / 4;
	private static final int EXTRACT_INTERVAL = 10;
	private static final int SOURCE_INTERVAL = 20;

	public static final class FluidTransit {
		public FluidVariant variant = FluidVariant.blank();
		public long amount;
		public Direction from = Direction.UP;
		public Direction to = Direction.DOWN;
		public float progress;
		public boolean turned;
	}

	private final List<FluidTransit> transit = new ArrayList<>();
	private int extractCooldown;
	private int sourceCooldown;

	public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.WATERPROOF_PIPE, pos, state);
	}

	public List<FluidTransit> transit() {
		return transit;
	}

	private boolean addTransit(FluidVariant variant, long amount, Direction from) {
		if (transit.size() >= MAX_TRANSIT || amount <= 0) {
			return false;
		}
		FluidTransit t = new FluidTransit();
		t.variant = variant;
		t.amount = amount;
		t.from = from;
		t.to = from.getOpposite();
		transit.add(t);
		setChanged();
		return true;
	}

	public void tick(Level level, BlockPos pos) {
		if (--extractCooldown <= 0) {
			extractCooldown = EXTRACT_INTERVAL;
			tryExtractStorage(level, pos);
		}
		if (--sourceCooldown <= 0) {
			sourceCooldown = SOURCE_INTERVAL;
			tryExtractSourceBlock(level, pos);
		}
		for (int i = transit.size() - 1; i >= 0; i--) {
			FluidTransit t = transit.get(i);
			t.progress += SPEED;
			if (!t.turned && t.progress >= 0.5F) {
				t.to = chooseExit(t.from);
				t.turned = true;
				setChanged();
			}
			if (t.progress >= 1.0F) {
				switch (handOff(level, pos, t)) {
				case REMOVE -> transit.remove(i);
				case BOUNCE, HOLD -> t.progress = Math.min(t.progress, 1.0F);
				}
				setChanged();
			}
		}
	}

	private enum HandOff {
		REMOVE, BOUNCE, HOLD
	}

	private Direction chooseExit(Direction from) {
		BlockState state = getBlockState();
		List<Direction> exits = new ArrayList<>();
		for (Direction d : Direction.values()) {
			if (d != from && state.getValue(BcnPipeBlock.property(d)) != Conn.NONE) {
				exits.add(d);
			}
		}
		return exits.isEmpty() ? from : exits.get(level.getRandom().nextInt(exits.size()));
	}

	private HandOff handOff(Level level, BlockPos pos, FluidTransit t) {
		BlockPos targetPos = pos.relative(t.to);
		BlockState targetState = level.getBlockState(targetPos);
		if (targetState.getBlock() instanceof BcnPipeBlock pipe && pipe.fluid
				&& level.getBlockEntity(targetPos) instanceof FluidPipeBlockEntity other) {
			if (other.transit.size() < MAX_TRANSIT) {
				t.from = t.to.getOpposite();
				t.to = t.from.getOpposite();
				t.progress = 0;
				t.turned = false;
				other.transit.add(t);
				other.setChanged();
				return HandOff.REMOVE;
			}
			return HandOff.HOLD;
		}
		Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, targetPos, t.to.getOpposite());
		if (storage != null) {
			long inserted;
			try (Transaction tx = Transaction.openOuter()) {
				inserted = storage.insert(t.variant, t.amount, tx);
				if (inserted > 0) {
					tx.commit();
				}
			}
			t.amount -= inserted;
			if (t.amount <= 0) {
				return HandOff.REMOVE;
			}
		}
		bounce(t);
		return HandOff.BOUNCE;
	}

	private static void bounce(FluidTransit t) {
		Direction back = t.from;
		t.from = t.to;
		t.to = back;
		t.progress = 0;
		t.turned = false;
	}

	private boolean hasExitBesides(Direction entry) {
		BlockState state = getBlockState();
		for (Direction d : Direction.values()) {
			if (d != entry && state.getValue(BcnPipeBlock.property(d)) != Conn.NONE) {
				return true;
			}
		}
		return false;
	}

	private void tryExtractStorage(Level level, BlockPos pos) {
		if (transit.size() >= MAX_TRANSIT) {
			return;
		}
		for (Direction d : Direction.values()) {
			if (!hasExitBesides(d)) {
				continue;
			}
			Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos.relative(d), d.getOpposite());
			if (storage == null) {
				continue;
			}
			try (Transaction tx = Transaction.openOuter()) {
				ResourceAmount<FluidVariant> content = StorageUtil.findExtractableContent(storage, tx);
				if (content == null) {
					continue;
				}
				long extracted = storage.extract(content.resource(), Math.min(content.amount(), EXTRACT_AMOUNT), tx);
				if (extracted > 0) {
					tx.commit();
					addTransit(content.resource(), extracted, d);
					return;
				}
			}
		}
	}

	private void tryExtractSourceBlock(Level level, BlockPos pos) {
		if (transit.size() >= MAX_TRANSIT) {
			return;
		}
		for (Direction d : Direction.values()) {
			if (!hasExitBesides(d)) {
				continue;
			}
			BlockPos sourcePos = pos.relative(d);
			FluidState fluid = level.getFluidState(sourcePos);
			if (fluid.isSource()) {
				level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
				addTransit(FluidVariant.of(fluid.getType()), BLOB_MAX, d);
				return;
			}
		}
	}

	public Storage<FluidVariant> storage(Direction face) {
		return new Storage<>() {
			@Override
			public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
				if (resource == null || resource.isBlank() || maxAmount <= 0 || transit.size() >= MAX_TRANSIT) {
					return 0;
				}
				long amount = Math.min(maxAmount, BLOB_MAX);
				transaction.addOuterCloseCallback(result -> {
					if (result.wasCommitted()) {
						addTransit(resource, amount, face);
					}
				});
				return amount;
			}

			@Override
			public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
				return 0;
			}

			@Override
			public Iterator<StorageView<FluidVariant>> iterator() {
				return Collections.emptyIterator();
			}
		};
	}

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		ValueOutput.ValueOutputList list = out.childrenList("transit");
		for (FluidTransit t : transit) {
			ValueOutput child = list.addChild();
			child.putString("fl", BuiltInRegistries.FLUID.getKey(t.variant.getFluid()).toString());
			child.putLong("amt", t.amount);
			child.putInt("f", t.from.ordinal());
			child.putInt("t", t.to.ordinal());
			child.putBoolean("r", t.turned);
			child.putFloat("p", t.progress);
		}
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		transit.clear();
		for (ValueInput child : in.childrenListOrEmpty("transit")) {
			Identifier fluidId = Identifier.tryParse(child.getStringOr("fl", ""));
			Fluid fluid = fluidId == null ? null : BuiltInRegistries.FLUID.getValue(fluidId);
			long amount = child.getLongOr("amt", 0);
			if (fluid == null || amount <= 0) {
				continue;
			}
			FluidTransit t = new FluidTransit();
			t.variant = FluidVariant.of(fluid);
			t.amount = amount;
			t.from = Direction.values()[child.getIntOr("f", 0)];
			t.to = Direction.values()[child.getIntOr("t", 0)];
			t.turned = child.getBooleanOr("r", false);
			t.progress = child.getFloatOr("p", 0);
			transit.add(t);
		}
	}
}
