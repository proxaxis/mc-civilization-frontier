package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.block.entity.PlanterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 自動栽培機. MJ 供給で周囲 5x5 の作物を自動栽培・収穫する.
 * 種アイテムを持って右クリックで種スロットへ投入、素手右クリックで状態表示.
 */
public class BcnPlanterBlock extends Block implements EntityBlock {

	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public BcnPlanterBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PlanterBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return type == MachineBlockEntities.PLANTER
				? (l, p, s, be) -> { if (be instanceof PlanterBlockEntity pe) PlanterBlockEntity.tick(l, p, s, pe); }
				: null;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof PlanterBlockEntity be) {
			if (!level.isClientSide() && be.insertSeed(stack)) {
				level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PlanterBlockEntity be) {
			player.sendSystemMessage(Component.literal(String.format(
					"[Planter] MJ %d/%d, 種 %d, 収穫物 %d",
					be.mjStored(), PlanterBlockEntity.MJ_CAPACITY, be.seedCount(), be.outputCount())));
		}
		return InteractionResult.SUCCESS;
	}
}
