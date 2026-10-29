package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 地熱発電機. 溶岩を消費して 8 MJ/t を発電する (IC2 地熱 20 EU/t -> 8 MJ/t).
 * 溶岩バケツまたは防水パイプから供給. 内部タンク 4 バケツ.
 */
public class GeothermalBlockEntity extends MjStorageBlockEntity {

	public static final double RATE = 8.0;
	public static final long TANK_CAPACITY = 81000L * 4;
	/** 1 バケツあたりの発生 MJ. */
	public static final double ENERGY_PER_BUCKET = 8000;
	private static final long DRAIN_PER_TICK = (long) (RATE * 81000L / ENERGY_PER_BUCKET); // 81

	private long tankAmount;
	private double producedTotal;

	public GeothermalBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.GEOTHERMAL_GENERATOR, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, GeothermalBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		boolean running = false;
		if (be.tankAmount >= DRAIN_PER_TICK && be.energy < be.capacity()) {
			be.tankAmount -= DRAIN_PER_TICK;
			be.energy = Math.min(be.capacity(), be.energy + RATE);
			be.producedTotal += RATE;
			running = true;
		}
		if (be.energy >= be.maxOutputPacket()) {
			be.emit(level);
		}
		if (state.getValue(MjMachineBlock.ACTIVE) != running) {
			level.setBlock(pos, state.setValue(MjMachineBlock.ACTIVE, running), Block.UPDATE_ALL);
		}
		if (running) {
			be.setChanged();
		}
	}

	public long tankAmount() {
		return tankAmount;
	}

	/** 溶岩投入用ストレージ (バケツ/防水パイプ). 回収不可. */
	public Storage<FluidVariant> fluidStorage(Direction face) {
		return new Storage<>() {
			@Override
			public long insert(FluidVariant resource, long maxAmount, TransactionContext tx) {
				if (resource == null || resource.isBlank() || maxAmount <= 0) {
					return 0;
				}
				if (resource.getFluid() != Fluids.LAVA && resource.getFluid() != Fluids.FLOWING_LAVA) {
					return 0;
				}
				long accepted = Math.min(TANK_CAPACITY - tankAmount, maxAmount);
				if (accepted > 0) {
					tx.addOuterCloseCallback(result -> {
						if (result.wasCommitted()) {
							tankAmount += accepted;
							setChanged();
						}
					});
				}
				return accepted;
			}

			@Override
			public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
				return 0;
			}

			@Override
			public java.util.Iterator<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<FluidVariant>> iterator() {
				return java.util.Collections.emptyIterator();
			}
		};
	}

	@Override
	protected double capacity() {
		return MjPressure.LV.packetSize() * 4.0;
	}

	@Override
	protected double maxInputPacket() {
		return 0;
	}

	@Override
	protected double maxOutputPacket() {
		return MjPressure.LV.packetSize();
	}

	@Override
	protected Direction outputFace(BlockState state) {
		return state.getValue(MjMachineBlock.FACING);
	}

	@Override
	protected int packetsPerTick() {
		return 1;
	}

	@Override
	protected boolean acceptsInput(Direction face) {
		return false;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putLong("lava", tankAmount);
		output.putDouble("produced_total", producedTotal);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		tankAmount = input.getLongOr("lava", 0);
		producedTotal = input.getDoubleOr("produced_total", 0);
	}
}
