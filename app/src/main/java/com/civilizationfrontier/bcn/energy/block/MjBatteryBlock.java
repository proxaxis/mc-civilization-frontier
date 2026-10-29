package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.entity.BatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 蓄電器. FACING 面が出力 (IC2 のドット面相当)、他5面が入力.
 */
public class MjBatteryBlock extends MjMachineBlock {

	private final MjPressure rank;
	private final double capacity;

	public MjBatteryBlock(MjPressure rank, double capacity, Properties properties) {
		super(properties);
		this.rank = rank;
		this.capacity = capacity;
	}

	public MjPressure rank() {
		return rank;
	}

	public double capacity() {
		return capacity;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BatteryBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return ticker(type, MachineBlockEntities.BATTERY, BatteryBlockEntity::tick);
	}
}
