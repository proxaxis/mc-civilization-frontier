package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 検出ケーブルの BlockEntity. パケット通過で短時間レッドストーン信号 (15) を出す.
 */
public class CableDetectorBlockEntity extends BlockEntity {

	private static final int PULSE_TICKS = 4;
	private int poweredTicks;

	public CableDetectorBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.CABLE_DETECTOR, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CableDetectorBlockEntity be) {
		if (level.isClientSide()) return;
		if (be.poweredTicks > 0 && --be.poweredTicks == 0) {
			be.setChanged();
			level.updateNeighborsAt(pos, state.getBlock());
		}
	}

	/** パケット通過時に呼ばれ、信号を PULSE_TICKS 分だけ延長する. */
	public void pulse() {
		boolean wasOff = poweredTicks <= 0;
		poweredTicks = PULSE_TICKS;
		setChanged();
		if (wasOff && level != null) {
			level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
		}
	}

	public int signal() {
		return poweredTicks > 0 ? 15 : 0;
	}
}
