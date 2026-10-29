package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.entity.TransformerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 変圧器. inputRank 以下のパケットを側面で受け、FACING 面から outputRank のパケットを出す.
 */
public class MjTransformerBlock extends MjMachineBlock {

	private final MjPressure inputRank;
	private final MjPressure outputRank;

	public MjTransformerBlock(MjPressure inputRank, MjPressure outputRank, Properties properties) {
		super(properties);
		this.inputRank = inputRank;
		this.outputRank = outputRank;
	}

	public MjPressure inputRank() {
		return inputRank;
	}

	public MjPressure outputRank() {
		return outputRank;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TransformerBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return ticker(type, MachineBlockEntities.TRANSFORMER, TransformerBlockEntity::tick);
	}
}
