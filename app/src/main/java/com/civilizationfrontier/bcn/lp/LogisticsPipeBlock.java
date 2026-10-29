package com.civilizationfrontier.bcn.lp;

import com.civilizationfrontier.bcn.block.BcnPipeBlock;
import com.civilizationfrontier.bcn.item.BcnWrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 物流パイプブロック. アイテム管と同じ接続モデルの上に物流機能を持つ.
 * 設定は NBT 駆動: 右クリックで手に持つアイテムをフィルタ/発注として登録する.
 * chassis はモジュールアイテムの右クリック挿入・レンチでの取出に対応する.
 */
public class LogisticsPipeBlock extends BcnPipeBlock {

	private final LpKind kind;

	public LogisticsPipeBlock(LpKind kind, Properties properties) {
		super(properties, false);
		this.kind = kind;
	}

	public LpKind kind() {
		return kind;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LogisticsPipeBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()
				|| !(level.getBlockEntity(pos) instanceof LogisticsPipeBlockEntity be)) {
			return InteractionResult.SUCCESS;
		}
		if (stack.getItem() instanceof LpModuleItem) {
			if (be.insertModule(stack)) {
				stack.shrink(1);
				player.sendOverlayMessage(Component.literal("module inserted"));
			} else {
				player.sendOverlayMessage(Component.literal("no free module slot"));
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.getItem() instanceof BcnWrenchItem) {
			ItemStack popped = be.popModule();
			if (popped.isEmpty()) {
				player.sendOverlayMessage(Component.literal("no module installed"));
			} else {
				Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, popped);
				player.sendOverlayMessage(Component.literal("module removed"));
			}
			return InteractionResult.SUCCESS;
		}
		switch (kind) {
		case REQUEST -> {
			be.setFilter(stack);
			player.sendOverlayMessage(Component.literal("request: " + stack.getCount() + "x "
					+ stack.getItem().toString()));
			return InteractionResult.SUCCESS;
		}
		case BASIC -> {
			be.setFilter(stack.copyWithCount(1));
			player.sendOverlayMessage(Component.literal("filter: " + stack.getItem().toString()));
			return InteractionResult.SUCCESS;
		}
		case SUPPLIER -> {
			be.setFilter(stack);
			player.sendOverlayMessage(Component.literal("supply: " + stack.getCount() + "x "
					+ stack.getItem().toString()));
			return InteractionResult.SUCCESS;
		}
		default -> {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
		}
	}
}
