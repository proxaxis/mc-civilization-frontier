package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.block.entity.AutoWorkbenchBlockEntity;
import com.civilizationfrontier.bcn.block.entity.FilteredBufferBlockEntity;
import com.civilizationfrontier.bcn.block.entity.FloodGateBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** 作業台・選別緩衝器・水門の共用ブロック (kind で BE と振る舞いを分岐). */
public class BcnSimpleMachineBlock extends Block implements EntityBlock {

	public enum Kind { AUTO_WORKBENCH, FILTERED_BUFFER, FLOOD_GATE }

	private final Kind kind;

	public BcnSimpleMachineBlock(Properties properties, Kind kind) {
		super(properties);
		this.kind = kind;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return switch (kind) {
		case AUTO_WORKBENCH -> new AutoWorkbenchBlockEntity(pos, state);
		case FILTERED_BUFFER -> new FilteredBufferBlockEntity(pos, state);
		case FLOOD_GATE -> new FloodGateBlockEntity(pos, state);
		};
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return (lvl, pos, st, be) -> {
			if (be instanceof AutoWorkbenchBlockEntity a) {
				AutoWorkbenchBlockEntity.tick(lvl, pos, st, a);
			} else if (be instanceof FilteredBufferBlockEntity f) {
				FilteredBufferBlockEntity.tick(lvl, pos, st, f);
			} else if (be instanceof FloodGateBlockEntity g) {
				FloodGateBlockEntity.tick(lvl, pos, st, g);
			}
		};
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (kind == Kind.FLOOD_GATE && level.getBlockEntity(pos) instanceof FloodGateBlockEntity be
				&& FluidStorageUtil.interactWithFluidStorage(be.fluidStorage(hit.getDirection()), player, hand)) {
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
}
