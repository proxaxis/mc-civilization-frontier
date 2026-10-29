package com.civilizationfrontier.bcn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Oil spring found at the bottom of large oil fields. Rarely seeps new
 * oil into the space above itself.
 */
public class OilSpringBlock extends Block {
	private static final int SEEP_CHANCE = 8;

	public OilSpringBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(SEEP_CHANCE) != 0) {
			return;
		}
		BlockPos above = pos.above();
		if (level.getBlockState(above).canBeReplaced()) {
			level.setBlock(above, BcnBlocks.OIL.defaultBlockState(), 3);
		}
	}
}
