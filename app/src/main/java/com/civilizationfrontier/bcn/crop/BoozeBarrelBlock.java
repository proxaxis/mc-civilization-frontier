package com.civilizationfrontier.bcn.crop;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 酒樽. 水と材料を入れて時間発酵させ、マグカップで汲み出す.
 *
 * - 水バケツ: 注水 (仕込みの前提).
 * - 砂糖/サトウキビ x3: ラム. 小麦 x2 + ホップ x1: 麦酒系.
 * - マグカップ右クリック: 発酵段階に応じた飲料を汲み出す (4 杯分).
 * - 素手右クリック: 状態表示.
 */
public class BoozeBarrelBlock extends Block implements EntityBlock {

	public BoozeBarrelBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BoozeBarrelBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return type == MachineBlockEntities.BOOZE_BARREL
				? (l, p, s, be) -> { if (be instanceof BoozeBarrelBlockEntity bb) BoozeBarrelBlockEntity.tick(l, p, s, bb); }
				: null;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof BoozeBarrelBlockEntity be)) {
			return InteractionResult.PASS;
		}

		// 注水
		if (stack.is(Items.WATER_BUCKET)) {
			if (!be.addWater()) return InteractionResult.PASS;
			stack.consume(1, player);
			if (!player.getInventory().add(new ItemStack(Items.BUCKET))) {
				Block.popResource(level, pos, new ItemStack(Items.BUCKET));
			}
			sendStatus(player, be, "注水");
			return InteractionResult.CONSUME;
		}

		// 材料投入
		if (stack.is(Items.SUGAR) || stack.is(Items.SUGAR_CANE)) {
			if (!be.addSugar()) return InteractionResult.PASS;
			stack.consume(1, player);
			sendStatus(player, be, "砂糖 +1");
			return InteractionResult.CONSUME;
		}
		if (stack.is(Items.WHEAT)) {
			if (!be.addWheat()) return InteractionResult.PASS;
			stack.consume(1, player);
			sendStatus(player, be, "小麦 +1");
			return InteractionResult.CONSUME;
		}
		if (stack.is(BcnItems.HOPS)) {
			if (!be.addHops()) return InteractionResult.PASS;
			stack.consume(1, player);
			sendStatus(player, be, "ホップ +1");
			return InteractionResult.CONSUME;
		}

		// マグカップで汲み出す
		if (stack.is(BcnItems.STONE_MUG)) {
			String drinkId = be.serveDrink();
			if (drinkId.isEmpty()) return InteractionResult.PASS;
			Item drink = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("bcn", drinkId));
			stack.consume(1, player);
			if (!player.getInventory().add(new ItemStack(drink))) {
				Block.popResource(level, pos, new ItemStack(drink));
			}
			level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			return InteractionResult.CONSUME;
		}

		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BoozeBarrelBlockEntity be) {
			sendStatus(player, be, "状態");
		}
		return InteractionResult.SUCCESS;
	}

	private void sendStatus(Player player, BoozeBarrelBlockEntity be, String action) {
		String brew = be.brewing()
				? String.format("%s (発酵 %d tick, 残り %d 杯)", be.brew(), be.fermentTicks(), be.servings())
				: String.format("未仕込み (水 %d, 砂糖 %d, 小麦 %d, ホップ %d)",
						be.waterStored(), be.sugar(), be.wheat(), be.hops());
		player.sendSystemMessage(Component.literal("[BoozeBarrel] " + action + ": " + brew));
	}
}
