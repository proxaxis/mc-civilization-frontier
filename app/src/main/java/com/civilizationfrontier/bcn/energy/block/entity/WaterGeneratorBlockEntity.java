package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 水力発電機. 3x3x3 範囲の水ブロック数に応じて最大 0.1 MJ/t を発電する.
 * IC2 ウォーターミル換算 (0.25 EU/t -> 0.1 MJ/t).
 */
public class WaterGeneratorBlockEntity extends EnvGeneratorBlockEntity {

	/** 水ブロック1個あたりの発電量 (MJ/t). 8個で上限に達する. */
	private static final double PER_WATER = 0.0125;
	private static final double MAX_RATE = 0.1;

	public WaterGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.WATER_GENERATOR, pos, state);
	}

	@Override
	protected double computeRate(Level level, BlockPos pos, BlockState state) {
		int water = 0;
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			if (level.getFluidState(p).is(FluidTags.WATER)) {
				water++;
			}
		}
		return Math.min(MAX_RATE, water * PER_WATER);
	}

	@Override
	protected MjPressure outputRank() {
		return MjPressure.ULV;
	}
}
