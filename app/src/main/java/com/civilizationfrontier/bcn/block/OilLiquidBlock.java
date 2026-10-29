package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.fluid.BcnFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.redstone.Orientation;

/**
 * Oil liquid block. Explodes shortly after being placed in ultra warm
 * dimensions and catches fire when fire or lava touches it.
 */
public class OilLiquidBlock extends LiquidBlock {
	private static final int NETHER_EXPLOSION_DELAY = 20;
	private static final float NETHER_EXPLOSION_POWER = 4.0F;

	public OilLiquidBlock(FlowingFluid fluid, Properties properties) {
		super(fluid, properties);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (isUltraWarm(level, pos)) {
			level.scheduleTick(pos, this, NETHER_EXPLOSION_DELAY);
			return;
		}
		super.onPlace(state, level, pos, oldState, movedByPiston);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			Orientation orientation, boolean movedByPiston) {
		if (isUltraWarm(level, pos)) {
			level.scheduleTick(pos, this, NETHER_EXPLOSION_DELAY);
			return;
		}
		tryIgnite(state, level, pos);
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (isUltraWarm(level, pos)) {
			level.removeBlock(pos, false);
			level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, NETHER_EXPLOSION_POWER,
					Level.ExplosionInteraction.BLOCK);
			return;
		}
		tryIgnite(state, level, pos);
		super.tick(state, level, pos, random);
	}

	private static boolean isUltraWarm(Level level, BlockPos pos) {
		return Boolean.TRUE.equals(level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos));
	}

	// Ignites oil that touches fire or lava. The source is consumed into a
	// shallow oil flow while fire keeps burning on top of it.
	private void tryIgnite(BlockState state, Level level, BlockPos pos) {
		boolean hot = false;
		for (Direction direction : Direction.values()) {
			BlockState neighbor = level.getBlockState(pos.relative(direction));
			if (neighbor.is(Blocks.FIRE) || neighbor.getFluidState().is(FluidTags.LAVA)) {
				hot = true;
				break;
			}
		}
		if (!hot) {
			return;
		}
		BlockPos above = pos.above();
		if (level.getBlockState(above).isAir()) {
			level.setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
		}
		if (state.getValue(LEVEL) == 0) {
			level.setBlock(pos, BcnFluids.FLOWING_OIL.getFlowing(7, false).createLegacyBlock(), 3);
		}
	}
}
