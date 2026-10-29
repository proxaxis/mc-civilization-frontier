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

/** 漏斗 (chute). 上の容器から 8 tick ごとに 1 個引き込み、下の容器/パイプへ押し出す. MJ 不要. */
public class ChuteBlockEntity extends BlockEntity implements WorldlyContainer {

	public static final int SIZE = 18;
	private static final int INTERVAL = 8;

	private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
	private int cooldown;

	public ChuteBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.CHUTE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ChuteBlockEntity be) {
		if (level.isClientSide() || --be.cooldown > 0) {
			return;
		}
		be.cooldown = INTERVAL;
		be.pull(level, pos);
		be.push(level, pos);
	}

	private void pull(Level level, BlockPos pos) {
		Container source = HopperBlockEntity.getContainerAt(level, pos.above());
		if (source == null) {
			return;
		}
		for (int i = 0; i < source.getContainerSize(); i++) {
			ItemStack stack = source.getItem(i);
			if (stack.isEmpty() || !source.canTakeItem(this, i, stack)) {
				continue;
			}
			ItemStack one = stack.copyWithCount(1);
			ItemStack rem = HopperBlockEntity.addItem(source, this, one, Direction.DOWN);
			if (rem.isEmpty()) {
				stack.shrink(1);
				source.setItem(i, stack);
				source.setChanged();
				return;
			}
		}
	}

	private void push(Level level, BlockPos pos) {
		Container target = HopperBlockEntity.getContainerAt(level, pos.below());
		if (target == null) {
			return;
		}
		for (int i = 0; i < SIZE; i++) {
			ItemStack stack = items.get(i);
			if (stack.isEmpty()) {
				continue;
			}
			ItemStack rem = HopperBlockEntity.addItem(this, target, stack, Direction.UP);
			items.set(i, rem);
			if (rem.isEmpty()) {
				setChanged();
				return;
			}
		}
	}

	@Override public int getContainerSize() { return SIZE; }
	@Override public boolean isEmpty() { for (ItemStack s : items) { if (!s.isEmpty()) return false; } return true; }
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
	@Override public int[] getSlotsForFace(Direction side) { int[] a = new int[SIZE]; for (int i = 0; i < SIZE; i++) a[i] = i; return a; }
	@Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) { return true; }
	@Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) { return dir == Direction.DOWN || dir == Direction.UP; }

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
