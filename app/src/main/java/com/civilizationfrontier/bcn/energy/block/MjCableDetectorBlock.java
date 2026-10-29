package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.entity.CableDetectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 検出ケーブル. パケットが通過するたび短時間レッドストーン信号 (15) を出す.
 * IC2 の Detector Cable 相当で、許容圧は EV (全圧を検出できる汎用ケーブル).
 */
public class MjCableDetectorBlock extends MjCableBlock {

	public MjCableDetectorBlock(Properties properties) {
		super(MjPressure.EV, 0.32, properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CableDetectorBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return type == MachineBlockEntities.CABLE_DETECTOR
				? (l, p, s, be) -> { if (be instanceof CableDetectorBlockEntity d) CableDetectorBlockEntity.tick(l, p, s, d); }
				: null;
	}

	@Override
	public void onPacketPass(Level level, BlockPos pos, double packetEnergy) {
		if (level.getBlockEntity(pos) instanceof CableDetectorBlockEntity be) {
			be.pulse();
		}
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
		return signal(level, pos);
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction dir) {
		return signal(level, pos);
	}

	private int signal(BlockGetter level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof CableDetectorBlockEntity be ? be.signal() : 0;
	}
}
