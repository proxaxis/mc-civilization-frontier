package com.civilizationfrontier.bcn.worldgen;

import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Generates an oil field: a surface pool of oil, a spout of oil rising
 * from the pool and an underground oil pocket that may contain an oil
 * spring at its bottom.
 *
 * <p>{@code pocket_depth} is the distance from the surface down to the
 * centre of the oil pocket. {@code spout_height} is the height of the
 * spout above the surface; the column itself always reaches down to the
 * pocket.
 */
public record OilFieldFeature(int poolRadius, int poolDepth, int spoutHeight, int pocketRadius, int pocketDepth,
		float springChance) implements Feature {
	public static final MapCodec<OilFieldFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.fieldOf("pool_radius").forGetter(OilFieldFeature::poolRadius),
			Codec.INT.fieldOf("pool_depth").forGetter(OilFieldFeature::poolDepth),
			Codec.INT.fieldOf("spout_height").forGetter(OilFieldFeature::spoutHeight),
			Codec.INT.fieldOf("pocket_radius").forGetter(OilFieldFeature::pocketRadius),
			Codec.INT.fieldOf("pocket_depth").forGetter(OilFieldFeature::pocketDepth),
			Codec.FLOAT.fieldOf("spring_chance").forGetter(OilFieldFeature::springChance))
			.apply(instance, OilFieldFeature::new));

	private static final BlockState OIL_STATE = BcnBlocks.OIL.defaultBlockState();

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int groundY = findGround(level, origin);
		if (groundY == Integer.MIN_VALUE) {
			return false;
		}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		placePocket(level, random, origin.getX(), groundY, origin.getZ());
		placePool(level, pos, origin.getX(), groundY, origin.getZ());
		placeSpout(level, pos, origin.getX(), groundY, origin.getZ());
		return true;
	}

	// Finds the topmost solid or liquid block at or below the origin.
	private int findGround(WorldGenLevel level, BlockPos origin) {
		BlockPos.MutableBlockPos pos = origin.mutable();
		int minY = Math.max(level.getMinY(), origin.getY() - 64);
		for (int y = origin.getY(); y >= minY; y--) {
			pos.setY(y);
			BlockState state = level.getBlockState(pos);
			if (!state.isAir() && !state.canBeReplaced() || !level.getFluidState(pos).isEmpty()) {
				return y;
			}
		}
		return Integer.MIN_VALUE;
	}

	private void placePool(WorldGenLevel level, BlockPos.MutableBlockPos pos, int cx, int groundY, int cz) {
		double radiusSq = poolRadius * poolRadius;
		for (int dx = -poolRadius; dx <= poolRadius; dx++) {
			for (int dz = -poolRadius; dz <= poolRadius; dz++) {
				double distSq = dx * dx + dz * dz;
				if (distSq > radiusSq) {
					continue;
				}
				int depth = 1 + (int) Math.round(poolDepth * (1.0 - distSq / radiusSq));
				for (int dy = 0; dy <= depth; dy++) {
					pos.set(cx + dx, groundY - dy, cz + dz);
					if (canHoldOil(level, pos)) {
						setBlock(level, pos, OIL_STATE);
					}
				}
			}
		}
	}

	private void placeSpout(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int groundY, int z) {
		int bottom = groundY - pocketDepth + pocketRadius;
		int top = groundY + spoutHeight;
		for (int y = bottom; y <= top; y++) {
			pos.set(x, y, z);
			BlockState state = level.getBlockState(pos);
			if (state.isAir() || !level.getFluidState(pos).isEmpty() || isOverworldBase(state)) {
				setBlock(level, pos, OIL_STATE);
			}
		}
	}

	private void placePocket(WorldGenLevel level, RandomSource random, int cx, int groundY, int cz) {
		int centerY = groundY - pocketDepth;
		if (centerY - pocketRadius < level.getMinY() + 1) {
			return;
		}
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		double radiusSq = pocketRadius * pocketRadius;
		for (int dx = -pocketRadius; dx <= pocketRadius; dx++) {
			for (int dy = -pocketRadius; dy <= pocketRadius; dy++) {
				for (int dz = -pocketRadius; dz <= pocketRadius; dz++) {
					if (dx * dx + dy * dy + dz * dz > radiusSq) {
						continue;
					}
					pos.set(cx + dx, centerY + dy, cz + dz);
					if (isOverworldBase(level.getBlockState(pos))) {
						setBlock(level, pos, OIL_STATE);
					}
				}
			}
		}
		if (random.nextFloat() < springChance) {
			pos.set(cx, centerY - pocketRadius - 1, cz);
			if (isOverworldBase(level.getBlockState(pos))) {
				setBlock(level, pos, BcnBlocks.OIL_SPRING.defaultBlockState());
			}
		}
	}

	private boolean canHoldOil(WorldGenLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || !level.getFluidState(pos).isEmpty() || isOverworldBase(state);
	}

	private static boolean isOverworldBase(BlockState state) {
		return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(Blocks.DIRT) || state.is(Blocks.GRAVEL)
				|| state.is(Blocks.SAND) || state.is(Blocks.SANDSTONE) || state.is(Blocks.CLAY)
				|| state.is(Blocks.MUD);
	}
}
