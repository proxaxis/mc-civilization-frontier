package com.civilizationfrontier.bcn.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 選別緩衝器. フィルター (0-8, ゴースト) に一致したアイテムのみ隣接容器/パイプへ押し出すバッファ (9-26). */
public class FilteredBufferBlockEntity extends BlockEntity implements WorldlyContainer {

	public static final int FILTER = 9;
	public static final int SIZE = 27;
	private static final int INTERVAL = 8;
	private static final int[] ALL_SLOTS = new int[SIZE];
	static { for (int i = 0; i < SIZE; i++) ALL_SLOTS[i] = i; }

	private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
	private int cooldown;

	public FilteredBufferBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.FILTERED_BUFFER, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, FilteredBufferBlockEntity be) {
		if (level.isClientSide() || --be.cooldown > 0) {
			return;
		}
		be.cooldown = INTERVAL;
		be.push(level, pos);
	}

	private boolean passes(ItemStack stack) {
		boolean any = false;
		for (int i = 0; i < FILTER; i++) {
			ItemStack f = items.get(i);
			if (f.isEmpty()) {
				continue;
			}
			any = true;
			if (ItemStack.isSameItemSameComponents(f, stack)) {
				return true;
			}
		}
		return !any;
	}

	private void push(Level level, BlockPos pos) {
		for (int slot = FILTER; slot < SIZE; slot++) {
			ItemStack stack = items.get(slot);
			if (stack.isEmpty() || !passes(stack)) {
				continue;
			}
			for (Direction d : Direction.values()) {
				Container target = HopperBlockEntity.getContainerAt(level, pos.relative(d));
				if (target == null || target == this) {
					continue;
				}
				ItemStack rem = HopperBlockEntity.addItem(this, target, stack, d.getOpposite());
				items.set(slot, rem);
				if (rem.isEmpty()) {
					setChanged();
					return;
				}
			}
		}
	}

	@Override public int getContainerSize() { return SIZE; }
	@Override public boolean isEmpty() { for (int i = FILTER; i < SIZE; i++) { if (!items.get(i).isEmpty()) return false; } return true; }
	@Override public ItemStack getItem(int slot) { return items.get(slot); }
	@Override public ItemStack removeItem(int slot, int amount) {
		ItemStack s = ContainerHelper.removeItem(items, slot, amount);
		if (!s.isEmpty()) setChanged();
		return s;
	}
	@Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
	@Override public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		if (stack.getCount() > getMaxStackSize(stack)) stack.setCount(getMaxStackSize(stack));
		setChanged();
	}
	@Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
	@Override public void clearContent() { items.clear(); }
	@Override public int[] getSlotsForFace(Direction side) { return ALL_SLOTS; }
	@Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) { return slot >= FILTER; }
	@Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) { return slot >= FILTER; }

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null) Containers.dropContents(level, pos, this);
	}

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		ContainerHelper.saveAllItems(out, items);
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		ContainerHelper.loadAllItems(in, items);
	}
}
