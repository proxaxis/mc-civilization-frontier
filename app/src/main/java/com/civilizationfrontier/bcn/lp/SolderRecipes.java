package com.civilizationfrontier.bcn.lp;

import java.util.List;

import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 溶接作業台のレシピ表 (TECH_ITEMS.md Dev571 準拠, チップは同種の歯車で代用).
 * 全レシピ共通で加工1回につき鉄インゴット1個を燃料として消費する.
 */
public final class SolderRecipes {

	public record Entry(List<ItemStack> ingredients, ItemStack result, double mj) {
	}

	public static final double MJ_COST = 320;

	public static final List<Entry> ENTRIES = List.of(
			new Entry(List.of(stack(Items.HOPPER, 8)), stack(BcnItems.NANO_HOPPER, 1), MJ_COST),
			new Entry(List.of(stack(BcnItems.MODULE_BLANK, 2), stack(Items.ENDER_PEARL, 1),
					stack(BcnItems.GEAR_IRON, 2)), stack(BcnItems.ITEM_CARD, 2), MJ_COST),
			new Entry(List.of(stack(Items.DIAMOND, 3), stack(BcnItems.GEAR_DIAMOND, 1)),
					stack(BcnItems.HUD_BOW, 1), MJ_COST),
			new Entry(List.of(stack(Items.GLASS, 1), stack(BcnItems.GEAR_DIAMOND, 1),
					stack(Items.GLOWSTONE_DUST, 7)), stack(BcnItems.HUD_GLASS, 1), MJ_COST),
			new Entry(List.of(stack(Items.DIAMOND, 2), stack(BcnItems.GEAR_DIAMOND, 1)),
					stack(BcnItems.HUD_NOSE, 1), MJ_COST),
			new Entry(List.of(stack(BcnItems.HUD_NOSE, 1), stack(BcnItems.HUD_GLASS, 2),
					stack(BcnItems.HUD_BOW, 2)), stack(BcnItems.HUD_GLASSES, 1), MJ_COST));

	private static ItemStack stack(net.minecraft.world.level.ItemLike item, int count) {
		return new ItemStack(item, count);
	}

	private SolderRecipes() {
	}
}
