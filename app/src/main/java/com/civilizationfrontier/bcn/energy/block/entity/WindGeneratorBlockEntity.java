package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 風力発電機. 高さと天候で 0-2 MJ/t. y=64 で発電開始、y=144 以上で最大.
 * 雨で 1.2 倍、雷雨で 1.5 倍. 上空が見えない場合は 0.
 */
public class WindGeneratorBlockEntity extends EnvGeneratorBlockEntity {

	private static final double MAX_RATE = 2.0;
	private static final int BASE_Y = 64;
	private static final int FULL_Y = 144;

	public WindGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.WIND_GENERATOR, pos, state);
	}

	@Override
	protected double computeRate(Level level, BlockPos pos, BlockState state) {
		if (level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) {
			return 0; // 上空が塞がれている
		}
		double height = Math.max(0, Math.min(1.0,
				(double) (pos.getY() - BASE_Y) / (FULL_Y - BASE_Y)));
		double weather = level.isThundering() ? 1.5 : level.isRaining() ? 1.2 : 1.0;
		return MAX_RATE * height * weather;
	}

	@Override
	protected MjPressure outputRank() {
		return MjPressure.ULV;
	}
}
