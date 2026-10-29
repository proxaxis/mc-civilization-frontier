package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.block.entity.EngineBlockEntity;
import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.block.EngineKind;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 燃焼エンジン. 燃料を消費して MJ を発生し FACING 面へ送出する.
 * レッドストーン信号で稼働/停止を切替 (BC エンジン準拠).
 */
public class BcnEngineBlock extends MjMachineBlock {

	private final EngineKind kind;

	public BcnEngineBlock(EngineKind kind, Properties properties) {
		super(properties);
		this.kind = kind;
	}

	public EngineKind kind() {
		return kind;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return super.getStateForPlacement(ctx)
				.setValue(ACTIVE, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EngineBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return ticker(type, MachineBlockEntities.ENGINE, EngineBlockEntity::tick);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof EngineBlockEntity be) {
			if (kind.fluidFueled()
					&& FluidStorageUtil.interactWithFluidStorage(be.fluidStorage(hit.getDirection()), player, hand)) {
				return InteractionResult.SUCCESS;
			}
			if (kind.fuelMode() == EngineKind.FuelMode.SOLID && EngineBlockEntity.fuelTicks(stack) > 0) {
				if (!level.isClientSide() && be.insertFuel(stack)) {
					return InteractionResult.SUCCESS;
				}
				return InteractionResult.CONSUME;
			}
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
}
