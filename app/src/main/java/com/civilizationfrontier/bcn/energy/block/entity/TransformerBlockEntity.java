package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import com.civilizationfrontier.bcn.energy.block.MjTransformerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 変圧器. 出力面 (FACING) 以外の面で入力ランク以下のパケットを受け、
 * 内部バッファから出力ランクのパケットを最大4個/tick 送出する (大1 -> 小4).
 */
public class TransformerBlockEntity extends MjStorageBlockEntity {

	public TransformerBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.TRANSFORMER, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TransformerBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		be.emit(level);
		be.updateActive(level, state);
	}

	private MjTransformerBlock block() {
		return (MjTransformerBlock) getBlockState().getBlock();
	}

	public MjPressure inputRank() {
		return block().inputRank();
	}

	public MjPressure outputRank() {
		return block().outputRank();
	}

	@Override
	protected double capacity() {
		return inputRank().packetSize() * 4.0;
	}

	@Override
	protected double maxInputPacket() {
		return inputRank().packetSize();
	}

	@Override
	protected double maxOutputPacket() {
		return outputRank().packetSize();
	}

	@Override
	protected Direction outputFace(BlockState state) {
		return state.getValue(MjMachineBlock.FACING);
	}

	@Override
	protected int packetsPerTick() {
		return 4;
	}
}
