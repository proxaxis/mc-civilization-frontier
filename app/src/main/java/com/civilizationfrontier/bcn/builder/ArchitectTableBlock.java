package com.civilizationfrontier.bcn.builder;

import com.civilizationfrontier.bcn.builder.FillerBlock.FillerBlockEntity;
import com.civilizationfrontier.bcn.builder.LandmarkBlock.BlockVolume;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** 製図机. landmark 範囲を snapshot アイテムに書き込む. */
public class ArchitectTableBlock extends Block {

	public ArchitectTableBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(stack.getItem() instanceof SnapshotItem snap)
				|| (snap.kind() != SnapshotItem.Kind.TEMPLATE && snap.kind() != SnapshotItem.Kind.BLUEPRINT)) {
			return super.useItemOn(stack, state, level, pos, player, hand, hit);
		}
		BlockVolume vol = FillerBlockEntity.findAdjacentVolume(level, pos);
		if (vol == null) {
			player.sendSystemMessage(Component.literal("No landmark volume found adjacent"));
			return InteractionResult.SUCCESS;
		}
		CompoundTag data = SnapshotItem.capture(level, vol, snap.kind() == SnapshotItem.Kind.BLUEPRINT);
		data.putIntArray("origin", new int[]{vol.minX(), vol.minY(), vol.minZ()});
		ItemStack written = stack.copyWithCount(1);
		SnapshotItem.write(written, data);
		stack.shrink(1);
		if (!player.getInventory().add(written)) {
			net.minecraft.world.Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), written);
		}
		int[] s = data.getIntArray("size").orElse(new int[3]);
		player.sendSystemMessage(Component.literal(
				"Captured " + s[0] + "x" + s[1] + "x" + s[2] + " to " + snap.kind().name().toLowerCase()));
		return InteractionResult.SUCCESS;
	}
}
