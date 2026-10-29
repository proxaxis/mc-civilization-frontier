package com.civilizationfrontier.bcn.block.entity;

import com.civilizationfrontier.bcn.energy.MjNetwork;
import com.civilizationfrontier.bcn.energy.MjPressure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * デバッグ用の無限 MJ 供給ブロック. 毎 tick 全 6 面へ LV パケットを送出する.
 * 隣接機械への直送にもケーブル網への供給にも使える. 発電機 (task-203) 実装までの検証用.
 */
public class MjSourceBlockEntity extends BlockEntity {

	public MjSourceBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.MJ_SOURCE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, MjSourceBlockEntity be) {
		if (level.isClientSide()) return;
		for (Direction dir : Direction.values()) {
			MjNetwork.sendPacket(level, pos, dir, MjPressure.LV.packetSize(), be);
		}
	}
}
