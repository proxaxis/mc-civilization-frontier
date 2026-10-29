package com.civilizationfrontier.bcn.item;

import com.civilizationfrontier.bcn.item.BcnComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 作物分析機. IC2 の Cropnalyzer 近似.
 *
 * - 支柱ブロックに使用: 作物・5 パラメータの鑑定表示 (ブロック側で処理).
 * - 手に持って使用: 反対の手の種袋を鑑定しステータスを表示する.
 */
public class BcnAnalyzerItem extends Item {

	public BcnAnalyzerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack bag = player.getItemInHand(other);
		if (!bag.is(BcnItems.CROP_SEED_BAG)) {
			if (!level.isClientSide()) {
				player.sendSystemMessage(Component.literal("[Cropnalyzer] 反対の手に種袋を持ってください"));
			}
			return InteractionResult.SUCCESS;
		}
		if (!level.isClientSide()) {
			bag.set(BcnComponents.CROP_SCANNED, true);
			String cropId = bag.getOrDefault(BcnComponents.CROP_ID, "unknown");
			player.sendSystemMessage(Component.literal(String.format(
					"[Cropnalyzer] %s — Growth %d, Gain %d, Resistance %d",
					cropId,
					bag.getOrDefault(BcnComponents.CROP_GROWTH, 1),
					bag.getOrDefault(BcnComponents.CROP_GAIN, 1),
					bag.getOrDefault(BcnComponents.CROP_RESISTANCE, 1))));
		}
		return InteractionResult.SUCCESS;
	}
}
