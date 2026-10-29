package com.civilizationfrontier.bcn.lp;

import java.util.List;

import com.civilizationfrontier.bcn.block.BcnPipeBlock;
import com.civilizationfrontier.bcn.block.entity.FluidPipeBlockEntity;
import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 液体補給パイプの BlockEntity.
 * {@code liquid} (流体 ID) と {@code amount} (維持量 droplets) を NBT で設定し、
 * 防水パイプ網内の他タンクから宛先タンクへ配送させる.
 * 配送は MJ 消費 (1 回の発送につき {@link #COST_PER_SEND}).
 */
public class LiquidSupplierPipeBlockEntity extends FluidPipeBlockEntity implements MjReceiver {

	public static final double POWER_CAPACITY = 128;
	public static final double COST_PER_SEND = 4;
	private static final int SUPPLY_INTERVAL = 40;
	private static final long SEND_MAX = FluidConstants.BUCKET * 4;

	private Fluid liquid;
	private long amount;
	private double power;
	private int supplyCd;

	public LiquidSupplierPipeBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.LIQUID_SUPPLIER, pos, state);
	}

	@Override
	public double receive(double packetEnergy, Direction face) {
		double accepted = Math.min(POWER_CAPACITY - power, packetEnergy);
		if (accepted <= 0) {
			return 0;
		}
		power += accepted;
		setChanged();
		return accepted;
	}

	@Override
	public void tick(Level level, BlockPos pos) {
		super.tick(level, pos);
		if (--supplyCd > 0) {
			return;
		}
		supplyCd = SUPPLY_INTERVAL;
		if (liquid == null || amount <= 0 || power < COST_PER_SEND) {
			return;
		}
		FluidVariant variant = FluidVariant.of(liquid);
		// 補給先: パイプ以外の隣接 FluidStorage (1 基のみ, 複数隣接は非対応)
		BlockPos targetPos = null;
		Direction targetFace = null;
		Storage<FluidVariant> target = null;
		for (Direction d : Direction.values()) {
			BlockPos rel = pos.relative(d);
			if (level.getBlockState(rel).getBlock() instanceof BcnPipeBlock) {
				continue;
			}
			Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, rel, d.getOpposite());
			if (storage != null) {
				targetPos = rel;
				targetFace = d;
				target = storage;
				break;
			}
		}
		if (target == null) {
			return;
		}
		long have = stored(target, variant);
		long deficit = amount - have - inFlight(level, pos, variant);
		if (deficit <= 0) {
			return;
		}
		long send = Math.min(deficit, SEND_MAX);
		for (BlockPos member : LogisticsNetwork.members(level, pos, true)) {
			if (send <= 0) {
				break;
			}
			if (!(level.getBlockEntity(member) instanceof FluidPipeBlockEntity sourcePipe)) {
				continue;
			}
			for (Direction d : Direction.values()) {
				BlockPos donorPos = member.relative(d);
				if (donorPos.equals(targetPos)) {
					continue;
				}
				Storage<FluidVariant> donor = FluidStorage.SIDED.find(level, donorPos, d.getOpposite());
				if (donor == null || stored(donor, variant) <= 0) {
					continue;
				}
				List<Direction> route = LogisticsNetwork.route(level, member, pos, true);
				if (route == null) {
					continue;
				}
				long extracted;
				try (Transaction tx = Transaction.openOuter()) {
					extracted = donor.extract(variant, send, tx);
					if (extracted > 0) {
						tx.commit();
					}
				}
				if (extracted <= 0) {
					continue;
				}
				if (sourcePipe.addTransit(variant, extracted, d, pos.asLong(), targetFace.ordinal(), route)) {
					send -= extracted;
					power -= COST_PER_SEND;
					setChanged();
				} else {
					// 発送失敗分はドナーへ返却
					try (Transaction tx = Transaction.openOuter()) {
						donor.insert(variant, extracted, tx);
						tx.commit();
					}
				}
			}
		}
	}

	/** 宛先に向かう途中の液体量 (重複発注の抑止). */
	private long inFlight(Level level, BlockPos pos, FluidVariant variant) {
		long sum = 0;
		for (BlockPos member : LogisticsNetwork.members(level, pos, true)) {
			BlockEntity be = level.getBlockEntity(member);
			if (be instanceof FluidPipeBlockEntity pipe) {
				for (FluidTransit t : pipe.transit()) {
					if (t.dest == pos.asLong() && t.variant.equals(variant)) {
						sum += t.amount;
					}
				}
			}
		}
		return sum;
	}

	private static long stored(Storage<FluidVariant> storage, FluidVariant variant) {
		long sum = 0;
		for (StorageView<FluidVariant> view : storage) {
			if (!view.isResourceBlank() && view.getResource().equals(variant)) {
				sum += view.getAmount();
			}
		}
		return sum;
	}

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		out.putDouble("power", power);
		if (liquid != null) {
			out.putString("liquid", BuiltInRegistries.FLUID.getKey(liquid).toString());
		}
		out.putLong("amount", amount);
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		power = in.getDoubleOr("power", 0);
		Identifier id = Identifier.tryParse(in.getStringOr("liquid", ""));
		liquid = id == null ? null : BuiltInRegistries.FLUID.getValue(id);
		amount = in.getLongOr("amount", 0);
	}
}
