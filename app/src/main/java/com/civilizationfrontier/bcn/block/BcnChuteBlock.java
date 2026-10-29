package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.block.entity.ChuteBlockEntity;
import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BcnChuteBlock extends Block implements EntityBlock {

	public BcnChuteBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ChuteBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return (lvl, pos, st, be) -> {
			if (be instanceof ChuteBlockEntity c) {
				ChuteBlockEntity.tick(lvl, pos, st, c);
			}
		};
	}
}
