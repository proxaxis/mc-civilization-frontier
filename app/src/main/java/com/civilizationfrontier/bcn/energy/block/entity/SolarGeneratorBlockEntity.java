package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * ソーラー発電機. 日中のみ最大 0.4 MJ/t, 天候で減衰する.
 * 太陽高度 (SKY_LIGHT_FACTOR 環境属性) と天候係数を乗算する.
 */
public class SolarGeneratorBlockEntity extends EnvGeneratorBlockEntity {

	private static final double MAX_RATE = 0.4;

	public SolarGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.SOLAR_GENERATOR, pos, state);
	}

	@Override
	protected double computeRate(Level level, BlockPos pos, BlockState state) {
		if (level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() > pos.getY()) {
			return 0; // 上空が塞がれている
		}
		Float factor = level.environmentAttributes()
				.getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, pos);
		if (factor == null || factor <= 0) {
			return 0;
		}
		double weather = level.isThundering() ? 0.2 : level.isRaining() ? 0.4 : 1.0;
		return MAX_RATE * factor * weather;
	}

	@Override
	protected MjPressure outputRank() {
		return MjPressure.ULV;
	}
}
