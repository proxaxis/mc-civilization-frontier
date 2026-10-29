package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.MjBatteryBlock;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 蓄電器. 出力面 (FACING, IC2 のドット面相当) 以外の面でランク以下のパケットを受けて蓄電し、
 * 出力面からランクサイズまでのパケットを1個/tick 放出する.
 */
public class BatteryBlockEntity extends MjStorageBlockEntity {

	public BatteryBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.BATTERY, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BatteryBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		be.emit(level);
		be.updateActive(level, state);
	}

	private MjBatteryBlock block() {
		return (MjBatteryBlock) getBlockState().getBlock();
	}

	public MjPressure rank() {
		return block().rank();
	}

	@Override
	protected double capacity() {
		return block().capacity();
	}

	@Override
	protected double maxInputPacket() {
		return rank().packetSize();
	}

	@Override
	protected double maxOutputPacket() {
		return rank().packetSize();
	}

	@Override
	protected Direction outputFace(BlockState state) {
		return state.getValue(MjMachineBlock.FACING);
	}

	@Override
	protected int packetsPerTick() {
		return 1;
	}
}
