package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.block.entity.RefineryBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * リファイナリー. 黒オイルを MJ 消費で金オイル (燃料) に精製する.
 * 入力タンク (oil) → 加工 → 出力タンク (fuel). FACING 面は MJ 出力面ではなく
 * 向き表示のみ (流体は全面で入出力).
 */
public class MjRefineryBlock extends MjMachineBlock {

	public MjRefineryBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RefineryBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return ticker(type, MachineBlockEntities.REFINERY, RefineryBlockEntity::tick);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof RefineryBlockEntity be
				&& FluidStorageUtil.interactWithFluidStorage(be.fluidStorage(hit.getDirection()), player, hand)) {
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
}
