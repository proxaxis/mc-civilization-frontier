package com.civilizationfrontier.bcn.item;

import com.civilizationfrontier.bcn.item.BcnComponents;
import com.civilizationfrontier.bcn.crop.BcnCropType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * 育種した種を保持する袋. 作物種と GGR ステータスをデータコンポーネントで保持する.
 * 未鑑定の袋はツールチップにステータスを表示せず、作物分析機で鑑定する.
 * 支柱に使用すると保持した品種・ステータスで植え付けられる.
 */
public class BcnSeedBagItem extends Item {

	public BcnSeedBagItem(Properties properties) {
		super(properties);
	}

	public static ItemStack of(BcnCropType type, int growth, int gain, int resistance, boolean scanned) {
		ItemStack stack = new ItemStack(BcnItems.CROP_SEED_BAG);
		stack.set(BcnComponents.CROP_ID, type.id());
		stack.set(BcnComponents.CROP_GROWTH, growth);
		stack.set(BcnComponents.CROP_GAIN, gain);
		stack.set(BcnComponents.CROP_RESISTANCE, resistance);
		stack.set(BcnComponents.CROP_SCANNED, scanned);
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
			Consumer<Component> consumer, TooltipFlag flag) {
		boolean scanned = stack.getOrDefault(BcnComponents.CROP_SCANNED, false);
		String cropId = stack.getOrDefault(BcnComponents.CROP_ID, "");
		if (!scanned || cropId.isEmpty()) {
			consumer.accept(Component.literal("未鑑定の種 (作物分析機で鑑定)").withStyle(ChatFormatting.GRAY));
			return;
		}
		consumer.accept(Component.literal("Crop: " + cropId).withStyle(ChatFormatting.GREEN));
		consumer.accept(Component.literal(String.format("Growth %d, Gain %d, Resistance %d",
				stack.getOrDefault(BcnComponents.CROP_GROWTH, 1),
				stack.getOrDefault(BcnComponents.CROP_GAIN, 1),
				stack.getOrDefault(BcnComponents.CROP_RESISTANCE, 1))).withStyle(ChatFormatting.GRAY));
	}
}
