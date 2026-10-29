package com.civilizationfrontier.bcn.lp;

import com.civilizationfrontier.bcn.lp.LogisticsNetwork.FaceContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * リモートオーダー. 物流パイプへ右クリックで紐付けし、
 * 空中右クリックで紐付け先パイプへ遠隔要求を出す.
 * 要求アイテムは反対側の手のアイテム、または custom_data の {@code req} に保持したもの.
 */
public class RemoteOrdererItem extends Item {

	public RemoteOrdererItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockEntity be = level.getBlockEntity(ctx.getClickedPos());
		if (!(be instanceof LogisticsPipeBlockEntity)) {
			return InteractionResult.PASS;
		}
		ItemStack orderer = ctx.getItemInHand();
		if (!level.isClientSide()) {
			BlockPos pos = ctx.getClickedPos();
			CustomData.update(DataComponents.CUSTOM_DATA, orderer, tag -> {
				CompoundTag pipe = tag.getCompoundOrEmpty("pipe");
				pipe.putString("dim", level.dimension().identifier().toString());
				pipe.putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
				tag.put("pipe", pipe);
			});
			Player player = ctx.getPlayer();
			if (player != null) {
				player.sendOverlayMessage(Component.literal("linked: " + pos.toShortString()));
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack orderer = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		CompoundTag tag = orderer.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		CompoundTag pipe = tag.getCompoundOrEmpty("pipe");
		if (pipe.isEmpty()) {
			player.sendOverlayMessage(Component.literal("not linked"));
			return InteractionResult.SUCCESS;
		}
		if (!level.dimension().identifier().toString().equals(pipe.getStringOr("dim", ""))) {
			player.sendOverlayMessage(Component.literal("wrong dimension"));
			return InteractionResult.SUCCESS;
		}
		int[] xyz = pipe.getIntArray("pos").orElse(new int[0]);
		if (xyz.length != 3) {
			player.sendOverlayMessage(Component.literal("not linked"));
			return InteractionResult.SUCCESS;
		}
		BlockPos target = new BlockPos(xyz[0], xyz[1], xyz[2]);
		if (!(level.getBlockEntity(target) instanceof LogisticsPipeBlockEntity)) {
			player.sendOverlayMessage(Component.literal("link target missing"));
			return InteractionResult.SUCCESS;
		}
		// 反対側の手のアイテムを優先し、なければ保存済み要求を使う
		ItemStack want = player.getItemInHand(hand == InteractionHand.MAIN_HAND
				? InteractionHand.OFF_HAND
				: InteractionHand.MAIN_HAND);
		if (!want.isEmpty()) {
			final ItemStack record = want.copy();
			CustomData.update(DataComponents.CUSTOM_DATA, orderer, t -> t.store("req", ItemStack.CODEC, record));
		} else {
			want = tag.read("req", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		}
		if (want.isEmpty()) {
			player.sendOverlayMessage(Component.literal("no request item"));
			return InteractionResult.SUCCESS;
		}
		FaceContainer attached = LogisticsNetwork.attachedContainer(level, target);
		int face = attached != null ? attached.face().ordinal() : LogisticsNetwork.FACE_EJECT;
		int sent = LogisticsNetwork.request(level, want, target, target, face);
		player.sendOverlayMessage(
				Component.literal("ordered " + sent + "x " + want.getItem().toString()));
		return sent > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
	}
}
