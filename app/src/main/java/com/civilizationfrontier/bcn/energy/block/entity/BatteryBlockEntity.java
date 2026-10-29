package com.civilizationfrontier.bcn.energy.block.entity;

import java.util.stream.IntStream;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.MjBatteryBlock;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import com.civilizationfrontier.bcn.item.MjBatteryItem;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 蓄電器. 出力面 (FACING, IC2 のドット面相当) 以外の面でランク以下のパケットを受けて蓄電し、
 * 出力面からランクサイズまでのパケットを1個/tick 放出する.
 *
 * 機械スロットは 2 個: スロット0=充電 (バッファ→充電式バッテリー),
 * スロット1=放電 (バッテリー→バッファ. 使い捨ては空になれば消滅).
 * WorldlyContainer 実装でホッパー・パイプからの搬入出も可能.
 */
public class BatteryBlockEntity extends MjStorageBlockEntity implements WorldlyContainer {

	public static final int CHARGE_SLOT = 0;
	public static final int DISCHARGE_SLOT = 1;

	private static final int[] UP_SLOTS = {CHARGE_SLOT};
	private static final int[] DOWN_SLOTS = {DISCHARGE_SLOT};
	private static final int[] SIDE_SLOTS = IntStream.range(0, 2).toArray();

	private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

	public BatteryBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.BATTERY, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BatteryBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		be.processBatteries();
		be.emit(level);
		be.updateActive(level, state);
	}

	/** 放電スロットのバッテリーからバッファへ吸い上げ、充電スロットへバッファから充電する. */
	private void processBatteries() {
		ItemStack discharge = items.get(DISCHARGE_SLOT);
		if (discharge.getItem() instanceof MjBatteryItem battery) {
			int moved = battery.extract(discharge,
					(int) Math.min(battery.rate(), Math.max(0, capacity() - energy)));
			energy += moved;
			if (!battery.rechargeable() && MjBatteryItem.stored(discharge) <= 0) {
				items.set(DISCHARGE_SLOT, ItemStack.EMPTY); // 使い捨て電池は消滅
			}
		}
		ItemStack charge = items.get(CHARGE_SLOT);
		if (charge.getItem() instanceof MjBatteryItem battery && battery.rechargeable()) {
			int moved = battery.insert(charge,
					(int) Math.min(battery.rate(), Math.max(0, energy)));
			energy -= moved;
		}
		setChanged();
	}

	private MjBatteryBlock block() {
		return (MjBatteryBlock) getBlockState().getBlock();
	}

	public MjPressure rank() {
		return block().rank();
	}

	@Override
	protected double capacity() {
		return block().capacity();
	}

	@Override
	protected double maxInputPacket() {
		return rank().packetSize();
	}

	@Override
	protected double maxOutputPacket() {
		return rank().packetSize();
	}

	@Override
	protected Direction outputFace(BlockState state) {
		return state.getValue(MjMachineBlock.FACING);
	}

	@Override
	protected int packetsPerTick() {
		return 1;
	}

	// --- Container ---

	@Override
	public int getContainerSize() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
		if (!stack.isEmpty()) setChanged();
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		if (stack.getCount() > getMaxStackSize(stack)) stack.setCount(getMaxStackSize(stack));
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
		if (!(stack.getItem() instanceof MjBatteryItem battery)) return false;
		return slot == CHARGE_SLOT ? battery.rechargeable() : true;
	}

	// --- WorldlyContainer ---

	@Override
	public int[] getSlotsForFace(Direction side) {
		return switch (side) {
			case UP -> UP_SLOTS;
			case DOWN -> DOWN_SLOTS;
			default -> SIDE_SLOTS;
		};
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	// --- BlockEntity ---

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ContainerHelper.loadAllItems(input, items);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null) Containers.dropContents(level, pos, this);
	}
}
