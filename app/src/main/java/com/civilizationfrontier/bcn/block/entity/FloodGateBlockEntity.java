package com.civilizationfrontier.bcn.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 水門. 受け取った流体を周囲の空き空間へソースブロックとして拡散配置する (タンクの逆動作). */
public class FloodGateBlockEntity extends BlockEntity {

	public static final long BUFFER = FluidConstants.BUCKET * 4;
	private static final int INTERVAL = 20;

	private FluidVariant variant = FluidVariant.blank();
	private long amount;
	private int cooldown;

	public FloodGateBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.FLOOD_GATE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, FloodGateBlockEntity be) {
		if (level.isClientSide() || --be.cooldown > 0) {
			return;
		}
		be.cooldown = INTERVAL;
		be.spread(level, pos);
	}

	private void spread(Level level, BlockPos pos) {
		if (amount < FluidConstants.BUCKET || variant.isBlank()) {
			return;
		}
		List<Direction> dirs = new ArrayList<>(List.of(Direction.values()));
		Collections.shuffle(dirs);
		for (Direction d : dirs) {
			BlockPos target = pos.relative(d);
			BlockState targetState = level.getBlockState(target);
			if (!targetState.isAir() && !targetState.canBeReplaced()) {
				continue;
			}
			Fluid fluid = variant.getFluid();
			if (!(fluid instanceof net.minecraft.world.level.material.FlowingFluid flowing)) {
				continue;
			}
			BlockState place = flowing.getSource().defaultFluidState().createLegacyBlock();
			level.setBlock(target, place, net.minecraft.world.level.block.Block.UPDATE_ALL);
			amount -= FluidConstants.BUCKET;
			if (amount <= 0) {
				variant = FluidVariant.blank();
			}
			setChanged();
			return;
		}
	}

	public Storage<FluidVariant> fluidStorage(Direction face) {
		FloodGateBlockEntity self = this;
		return new Storage<>() {
			@Override
			public long insert(FluidVariant resource, long maxAmount, TransactionContext tx) {
				if (resource == null || resource.isBlank() || maxAmount <= 0
						|| (!variant.isBlank() && !variant.equals(resource))) {
					return 0;
				}
				long accepted = Math.min(BUFFER - amount, maxAmount);
				if (accepted > 0) {
					tx.addOuterCloseCallback(r -> {
						if (r.wasCommitted()) {
							variant = resource;
							amount += accepted;
							setChanged();
						}
					});
				}
				return accepted;
			}
			@Override public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) { return 0; }
			@Override public Iterator<StorageView<FluidVariant>> iterator() { return Collections.emptyIterator(); }
		};
	}

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		out.putString("fl", variant.isBlank() ? "" : BuiltInRegistries.FLUID.getKey(variant.getFluid()).toString());
		out.putLong("amt", amount);
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		String id = in.getStringOr("fl", "");
		Identifier fid = id.isEmpty() ? null : Identifier.tryParse(id);
		Fluid fluid = fid == null ? null : BuiltInRegistries.FLUID.getValue(fid);
		variant = fluid == null ? FluidVariant.blank() : FluidVariant.of(fluid);
		amount = fluid == null ? 0 : in.getLongOr("amt", 0);
	}
}
