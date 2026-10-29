package com.civilizationfrontier.bcn.lp;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 物流制御パイプ (chassis) に挿入するモジュールアイテム.
 * モジュール個別の設定 (フィルタ・個数) はスタックの custom_data
 * {@code {flt:<ItemStack>}} に保持し、chassis 側が参照する.
 */
public class LpModuleItem extends Item {

	private final LpModule kind;

	public LpModuleItem(LpModule kind, Properties properties) {
		super(properties);
		this.kind = kind;
	}

	public LpModule kind() {
		return kind;
	}

	/** モジュールスタックの種別. 非モジュール/空なら null. */
	public static LpModule kindOf(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof LpModuleItem module) {
			return module.kind();
		}
		return null;
	}

	/** モジュールのフィルタ設定. 未設定なら EMPTY. */
	public static ItemStack filterOf(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return ItemStack.EMPTY;
		}
		CompoundTag tag = data.copyTag();
		return tag.read("flt", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
	}

	/** フィルタ設定をモジュールスタックへ書き込む (in-place). */
	public static void setFilter(ItemStack module, ItemStack filter) {
		CustomData.update(DataComponents.CUSTOM_DATA, module, tag -> {
			if (filter.isEmpty()) {
				tag.remove("flt");
			} else {
				tag.store("flt", ItemStack.CODEC, filter);
			}
		});
	}
}
