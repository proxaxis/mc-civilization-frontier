package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.block.entity.QuarryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** クァーリー. 設置者を記録し、直下 5x5 を岩盤まで層状に採掘する. */
public class MjQuarryBlock extends MjMachineBlock {

	public MjQuarryBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new QuarryBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return ticker(type, MachineBlockEntities.QUARRY, QuarryBlockEntity::tick);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (placer != null && level.getBlockEntity(pos) instanceof QuarryBlockEntity be) {
			be.setOwner(placer.getUUID());
		}
	}
}
