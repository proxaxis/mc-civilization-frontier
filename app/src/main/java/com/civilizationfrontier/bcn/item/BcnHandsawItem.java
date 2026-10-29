package com.civilizationfrontier.bcn.item;

import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.civilizationfrontier.bcn.block.BcnMicroblockBlock;
import com.civilizationfrontier.bcn.block.entity.MicroblockBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

/**
 * 手ノコ. ブロックをマイクロブロックに分割するツール (PR の handsaw).
 *
 * - フルブロック (BlockEntity を持たない等方ブロック): HALF (1/2) に置換し、
 *   もう半分をマイクロブロックアイテムとして渡す
 * - マイクロブロック: HALF → PANELx2, PANEL → COVERx2 に分割して渡す
 *   (COVER はこれ以上割れない)
 * - 高純度シリコン (boule) ブロック: 宝石ノコのみ シリコンウェハー x8 に切断
 */
public class BcnHandsawItem extends Item {

	private final boolean gem;

	public BcnHandsawItem(boolean gem, Properties properties) {
		super(properties.stacksTo(1));
		this.gem = gem;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Player player = ctx.getPlayer();

		if (state.is(BcnBlocks.BOULE)) {
			if (!gem) {
				if (!level.isClientSide() && player != null) {
					player.sendOverlayMessage(
							net.minecraft.network.chat.Component.literal("need a gem handsaw"));
				}
				return InteractionResult.FAIL;
			}
			if (!level.isClientSide()) {
				level.removeBlock(pos, false);
				give(player, level, pos, new ItemStack(BcnItems.SILICON, 8));
				level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.5F, 1.2F);
			}
			return InteractionResult.SUCCESS;
		}

		if (state.getBlock() instanceof BcnMicroblockBlock) {
			BcnMicroblockBlock.MicroSize size = state.getValue(BcnMicroblockBlock.SIZE);
			BcnMicroblockBlock.MicroSize next = size.next();
			if (next == null) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				Block material = level.getBlockEntity(pos) instanceof MicroblockBlockEntity mb
						? mb.material() : net.minecraft.world.level.block.Blocks.STONE;
				level.removeBlock(pos, false);
				give(player, level, pos, MicroblockItem.of(material, next));
				give(player, level, pos, MicroblockItem.of(material, next));
				level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.4F, 1.5F);
			}
			return InteractionResult.SUCCESS;
		}

		if (!state.isCollisionShapeFullBlock(level, pos) || level.getBlockEntity(pos) != null
				|| state.getPistonPushReaction() == net.minecraft.world.level.material.PushReaction.IMMOVEABLE) {
			return InteractionResult.PASS;
		}

		// フルブロック → HALF (下半分のスラブ). 床に支持が無ければクリック面へ貼る
		Direction anchor = Direction.DOWN;
		AttachFace face = AttachFace.FLOOR;
		if (!FaceAttachedHorizontalDirectionalBlock.canAttach(level, pos, Direction.DOWN)) {
			anchor = ctx.getClickedFace();
			face = switch (anchor) {
				case DOWN -> AttachFace.CEILING;
				case UP -> AttachFace.FLOOR;
				default -> AttachFace.WALL;
			};
			if (!FaceAttachedHorizontalDirectionalBlock.canAttach(level, pos, anchor)) {
				return InteractionResult.PASS;
			}
		}
		if (!level.isClientSide()) {
			BlockState slab = BcnBlocks.MICROBLOCK.defaultBlockState()
					.setValue(BcnMicroblockBlock.SIZE, BcnMicroblockBlock.MicroSize.HALF)
					.setValue(FaceAttachedHorizontalDirectionalBlock.FACE, face);
			if (face == AttachFace.WALL) {
				slab = slab.setValue(FaceAttachedHorizontalDirectionalBlock.FACING, anchor);
			}
			level.setBlock(pos, slab, Block.UPDATE_ALL);
			if (level.getBlockEntity(pos) instanceof MicroblockBlockEntity mb) {
				mb.setMaterial(state.getBlock());
			}
			give(player, level, pos, MicroblockItem.of(state.getBlock(), BcnMicroblockBlock.MicroSize.HALF));
			level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.5F, 1.0F);
		}
		return InteractionResult.SUCCESS;
	}

	private static void give(Player player, Level level, BlockPos pos, ItemStack stack) {
		if (player != null && player.getInventory().add(stack)) {
			return;
		}
		Block.popResource(level, pos, stack);
	}
}
