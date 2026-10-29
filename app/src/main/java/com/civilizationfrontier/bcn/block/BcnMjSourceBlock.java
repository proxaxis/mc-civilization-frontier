package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.block.entity.MjSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** デバッグ用の無限 MJ 供給ブロック. 発電機実装までの検証用. */
public class BcnMjSourceBlock extends Block implements EntityBlock {

	public BcnMjSourceBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MjSourceBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return type == MachineBlockEntities.MJ_SOURCE
				? (l, p, s, be) -> { if (be instanceof MjSourceBlockEntity mbe) MjSourceBlockEntity.tick(l, p, s, mbe); }
				: null;
	}
}
