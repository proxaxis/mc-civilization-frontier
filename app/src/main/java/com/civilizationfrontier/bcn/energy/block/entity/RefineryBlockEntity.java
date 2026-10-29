package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.fluid.BcnFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * リファイナリー. 入力タンクの黒オイルを MJ 消費で金オイルに変換し出力タンクへ送る.
 * MJ は全面から受給 (LV 以下), 流体は入力=oil / 出力=fuel の一方通行.
 */
public class RefineryBlockEntity extends MjStorageBlockEntity {

	public static final long TANK_CAPACITY = 81000L * 8;  // 各タンク 8 バケツ
	public static final double MJ_PER_TICK = 4.0;         // 消費レート
	public static final long RATE_DROPLETS = 405;         // 変換量/稼働 tick (200t ≒ 1B, 800MJ/B)

	private static final double EFP_PER_TICK = 2.8e-6;

	private long oilAmount;
	private long fuelAmount;
	private double convertedTotal;

	public RefineryBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.REFINERY, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, RefineryBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		boolean running = false;
		if (be.energy >= MJ_PER_TICK && be.oilAmount > 0 && be.fuelAmount < TANK_CAPACITY) {
			long convertible = Math.min(be.oilAmount, TANK_CAPACITY - be.fuelAmount);
			long step = Math.min(convertible, RATE_DROPLETS);
			if (step > 0) {
				be.energy -= MJ_PER_TICK;
				be.oilAmount -= step;
				be.fuelAmount += step;
				be.convertedTotal += step / 81000.0;
				running = true;
				be.setChanged();
			}
		}
		if (running) {
			be.efpBuffer += EFP_PER_TICK;
			if (be.efpBuffer >= 1.0) {
				long whole = (long) be.efpBuffer;
				be.efpBuffer -= whole;
				BcnEfp.add(level.getServer(), whole);
			}
		}
		be.updateActive(level, state);
	}

	public long oilAmount() {
		return oilAmount;
	}

	public long fuelAmount() {
		return fuelAmount;
	}

	private static boolean isOil(FluidVariant v) {
		Fluid f = v.getFluid();
		return f == BcnFluids.OIL || f == BcnFluids.FLOWING_OIL;
	}

	private static boolean isFuel(FluidVariant v) {
		Fluid f = v.getFluid();
		return f == BcnFluids.FUEL || f == BcnFluids.FLOWING_FUEL;
	}

	/** タンクへの片面窓口. input=true なら注入のみ (oil 限定), false なら取出のみ (fuel). */
	private StorageView<FluidVariant> tankView(boolean input) {
		return new StorageView<>() {
			@Override
			public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
				if (input || !isFuel(resource) || maxAmount <= 0) {
					return 0;
				}
				long taken = Math.min(fuelAmount, maxAmount);
				if (taken > 0) {
					tx.addOuterCloseCallback(r -> {
						if (r.wasCommitted()) {
							fuelAmount -= taken;
							setChanged();
						}
					});
				}
				return taken;
			}

			@Override
			public boolean isResourceBlank() {
				return (input ? oilAmount : fuelAmount) <= 0;
			}

			@Override
			public FluidVariant getResource() {
				return input ? FluidVariant.of(BcnFluids.OIL) : FluidVariant.of(BcnFluids.FUEL);
			}

			@Override
			public long getAmount() {
				return input ? oilAmount : fuelAmount;
			}

			@Override
			public long getCapacity() {
				return TANK_CAPACITY;
			}
		};
	}

	/** 流体入出力窓口. 注入は oil→入力タンクのみ, 取出は出力タンクの fuel のみ. */
	public Storage<FluidVariant> fluidStorage(Direction face) {
		RefineryBlockEntity self = this;
		return new Storage<>() {
			@Override
			public long insert(FluidVariant resource, long maxAmount, TransactionContext tx) {
				if (resource == null || resource.isBlank() || !isOil(resource) || maxAmount <= 0) {
					return 0;
				}
				long accepted = Math.min(TANK_CAPACITY - oilAmount, maxAmount);
				if (accepted > 0) {
					tx.addOuterCloseCallback(r -> {
						if (r.wasCommitted()) {
							oilAmount += accepted;
							setChanged();
						}
					});
				}
				return accepted;
			}

			@Override
			public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
				return tankView(false).extract(resource, maxAmount, tx);
			}

			@Override
			public Iterator<StorageView<FluidVariant>> iterator() {
				List<StorageView<FluidVariant>> views = new ArrayList<>(2);
				views.add(tankView(true));
				views.add(tankView(false));
				return views.iterator();
			}
		};
	}

	@Override
	protected double capacity() {
		return MjPressure.LV.packetSize() * 20.0; // 260 MJ
	}

	@Override
	protected double maxInputPacket() {
		return MjPressure.LV.packetSize();
	}

	@Override
	protected double maxOutputPacket() {
		return 0;
	}

	@Override
	protected Direction outputFace(BlockState state) {
		return Direction.NORTH; // MJ 出力面なし (受電専用)
	}

	@Override
	protected int packetsPerTick() {
		return 0;
	}

	@Override
	protected boolean acceptsInput(Direction face) {
		return true; // 全面受電
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("oil_amount", oilAmount);
		output.putLong("fuel_amount", fuelAmount);
		output.putDouble("converted_total", convertedTotal);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		oilAmount = input.getLongOr("oil_amount", 0);
		fuelAmount = input.getLongOr("fuel_amount", 0);
		convertedTotal = input.getDoubleOr("converted_total", 0);
	}
}
