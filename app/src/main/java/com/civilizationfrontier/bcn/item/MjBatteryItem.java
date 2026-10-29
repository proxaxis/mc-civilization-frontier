package com.civilizationfrontier.bcn.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * MJ をアイテム内に保持するバッテリー系アイテム.
 * 充電量は {@link BcnComponents#MJ_STORED} コンポーネントに保持し、
 * 蓄電器の機械スロット経由で充放電する. `rechargeable=false` は使い捨て
 * (SU Battery 相当) で、充電は受け付けず空になれば消滅する.
 */
public class MjBatteryItem extends Item {

	private final int capacity;
	private final int rate;
	private final boolean rechargeable;
	private final int barColor;

	public MjBatteryItem(int capacity, int rate, boolean rechargeable, int barColor, Properties properties) {
		super(properties.stacksTo(1));
		this.capacity = capacity;
		this.rate = rate;
		this.rechargeable = rechargeable;
		this.barColor = barColor;
	}

	public int capacity() {
		return capacity;
	}

	/** 1 tick に充放電できる上限 (MJ). */
	public int rate() {
		return rate;
	}

	public boolean rechargeable() {
		return rechargeable;
	}

	public static int stored(ItemStack stack) {
		return stack.getOrDefault(BcnComponents.MJ_STORED, 0);
	}

	/** 充電. 実際に受け入れた量を返す. 使い捨ては 0. */
	public int insert(ItemStack stack, int amount) {
		if (!rechargeable) return 0;
		int accepted = Math.min(capacity - stored(stack), Math.max(0, amount));
		if (accepted > 0) {
			stack.set(BcnComponents.MJ_STORED, stored(stack) + accepted);
		}
		return accepted;
	}

	/** 放電. 実際に取り出せた量を返す. */
	public int extract(ItemStack stack, int amount) {
		int drained = Math.min(stored(stack), Math.max(0, amount));
		if (drained > 0) {
			stack.set(BcnComponents.MJ_STORED, stored(stack) - drained);
		}
		return drained;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * stored(stack) / capacity);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return barColor;
	}
}
