package com.civilizationfrontier.bcn.lp;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 溶接作業台の BlockEntity.
 * スロット構成: 0-8 = 材料, 9 = 燃料 (鉄インゴット), 10 = 完成品 (取出専用).
 * 材料集合がレシピに一致し、燃料と MJ が足りれば自動で組み立てる.
 */
public class SolderingStationBlockEntity extends BlockEntity implements WorldlyContainer, MjReceiver {

	public static final double POWER_CAPACITY = 1024;
	private static final int SIZE = 11;
	private static final int FUEL_SLOT = 9;
	private static final int OUT_SLOT = 10;
	private static final int WORK_INTERVAL = 10;

	private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
	private double power;
	private int workCd;

	public SolderingStationBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.SOLDERING_STATION, pos, state);
	}

	@Override
	public double receive(double packetEnergy, Direction face) {
		double accepted = Math.min(POWER_CAPACITY - power, packetEnergy);
		if (accepted <= 0) {
			return 0;
		}
		power += accepted;
		setChanged();
		return accepted;
	}

	public void tick(Level level, BlockPos pos) {
		if (--workCd > 0) {
			return;
		}
		workCd = WORK_INTERVAL;
		SolderRecipes.Entry recipe = match();
		if (recipe == null) {
			return;
		}
		ItemStack fuel = items.get(FUEL_SLOT);
		if (!fuel.is(Items.IRON_INGOT) || power < recipe.mj()) {
			return;
		}
		ItemStack out = items.get(OUT_SLOT);
		if (!out.isEmpty()
				&& (!ItemStack.isSameItemSameComponents(out, recipe.result())
						|| out.getCount() + recipe.result().getCount() > out.getMaxStackSize())) {
			return;
		}
		for (ItemStack ing : recipe.ingredients()) {
			removeCount(ing, ing.getCount());
		}
		fuel.shrink(1);
		power -= recipe.mj();
		if (out.isEmpty()) {
			items.set(OUT_SLOT, recipe.result().copy());
		} else {
			out.grow(recipe.result().getCount());
		}
		setChanged();
	}

	private SolderRecipes.Entry match() {
		outer: for (SolderRecipes.Entry e : SolderRecipes.ENTRIES) {
			for (ItemStack ing : e.ingredients()) {
				if (countOf(ing) < ing.getCount()) {
					continue outer;
				}
			}
			return e;
		}
		return null;
	}

	private int countOf(ItemStack match) {
		int n = 0;
		for (int i = 0; i < FUEL_SLOT; i++) {
			ItemStack s = items.get(i);
			if (ItemStack.isSameItemSameComponents(s, match)) {
				n += s.getCount();
			}
		}
		return n;
	}

	private void removeCount(ItemStack match, int count) {
		for (int i = 0; i < FUEL_SLOT && count > 0; i++) {
			ItemStack s = items.get(i);
			if (!ItemStack.isSameItemSameComponents(s, match)) {
				continue;
			}
			int take = Math.min(s.getCount(), count);
			s.shrink(take);
			if (s.isEmpty()) {
				items.set(i, ItemStack.EMPTY);
			}
			count -= take;
		}
	}

	/** 手持ちアイテムを材料/燃料スロットへ入れる (右クリック投入). */
	public boolean manualInsert(ItemStack held) {
		if (held.isEmpty()) {
			return false;
		}
		int slot = held.is(Items.IRON_INGOT) ? FUEL_SLOT : firstInputSlotFor(held);
		if (slot < 0) {
			return false;
		}
		ItemStack cur = items.get(slot);
		if (cur.isEmpty()) {
			items.set(slot, held.copyWithCount(1));
		} else {
			cur.grow(1);
		}
		held.shrink(1);
		setChanged();
		return true;
	}

	private int firstInputSlotFor(ItemStack held) {
		int free = -1;
		for (int i = 0; i < FUEL_SLOT; i++) {
			ItemStack s = items.get(i);
			if (ItemStack.isSameItemSameComponents(s, held) && s.getCount() < s.getMaxStackSize()) {
				return i;
			}
			if (s.isEmpty() && free < 0) {
				free = i;
			}
		}
		return free;
	}

	// ---- Container ----

	@Override
	public int getContainerSize() {
		return SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack s : items) {
			if (!s.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return ContainerHelper.removeItem(items, slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		items.clear();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot == FUEL_SLOT) {
			return stack.is(Items.IRON_INGOT);
		}
		return slot < FUEL_SLOT;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, FUEL_SLOT, OUT_SLOT };
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		return canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return slot == OUT_SLOT;
	}

	// ---- NBT ----

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		out.putDouble("power", power);
		ContainerHelper.saveAllItems(out, items);
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		power = in.getDoubleOr("power", 0);
		ContainerHelper.loadAllItems(in, items);
	}
}
