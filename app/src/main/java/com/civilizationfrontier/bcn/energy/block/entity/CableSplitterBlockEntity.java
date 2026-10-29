package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjCable;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 分割ケーブルの BlockEntity. 指定入力面から入ったパケットを、
 * 利用可能な送出レーンへラウンドロビンで振り分ける.
 */
public class CableSplitterBlockEntity extends BlockEntity {

	private int laneIndex;

	public CableSplitterBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.CABLE_SPLITTER, pos, state);
	}

	/**
	 * 入力面以外の送出可能レーン (ケーブル or MjReceiver が隣接する面) を
	 * パケットごとに巡回して返す. 送出先が無ければ null.
	 */
	public Direction nextLane(Level level, BlockPos pos, Direction inputFace) {
		Direction[] dirs = Direction.values();
		for (int i = 0; i < dirs.length; i++) {
			Direction d = dirs[(laneIndex + i) % dirs.length];
			if (d == inputFace) continue;
			BlockPos next = pos.relative(d);
			if (level.getBlockState(next).getBlock() instanceof MjCable
					|| level.getBlockEntity(next) instanceof MjReceiver) {
				laneIndex = (laneIndex + i + 1) % dirs.length;
				setChanged();
				return d;
			}
		}
		return null;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("lane_index", laneIndex);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		laneIndex = input.getIntOr("lane_index", 0);
	}
}
