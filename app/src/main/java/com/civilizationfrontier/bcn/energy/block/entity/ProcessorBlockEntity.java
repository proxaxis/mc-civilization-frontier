package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.BcnProcessingRecipes;
import com.civilizationfrontier.bcn.energy.BcnProcessingRecipes.Recipe;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import com.civilizationfrontier.bcn.energy.block.MjProcessorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * IC2 系加工機の共通 BlockEntity.
 * 入力 2 スロット (slot 0-1) + 出力 1 スロット (slot 2). レシピは
 * BcnProcessingRecipes が機種名で解決し、稼働中は MJ を消費して progress を進める.
 */
public class ProcessorBlockEntity extends BlockEntity implements WorldlyContainer, MjReceiver {

	public static final double CAPACITY = 260.0;
	public static final double EFP_PER_TICK = 2.8e-6;

	private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
	private double energy;
	private double efpBuffer;
	private int progress;
	private Recipe active;

	public ProcessorBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.PROCESSOR, pos, state);
	}

	public String machine() {
		return getBlockState().getBlock() instanceof MjProcessorBlock b ? b.machine() : "";
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ProcessorBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		boolean running = false;
		if (be.active == null) {
			Recipe found = BcnProcessingRecipes.find(be.machine(), be.items);
			if (found != null && be.canOutput(found)) {
				be.active = found;
				be.progress = 0;
			}
		}
		if (be.active != null) {
			if (be.energy >= be.active.mjPerTick() && be.canOutput(be.active)) {
				be.energy -= be.active.mjPerTick();
				be.progress++;
				running = true;
				if (be.progress >= be.active.ticks()) {
					BcnProcessingRecipes.consume(be.active, be.items);
					be.produce(be.active);
					be.active = null;
					be.progress = 0;
				}
				be.setChanged();
			}
		}
		if (running) {
			be.efpBuffer += EFP_PER_TICK;
			if (be.efpBuffer >= 1.0) {
				long whole = (long) be.efpBuffer;
				be.efpBuffer -= whole;
				BcnEfp.add(level.getServer(), whole);
			}
		}
		boolean active = running;
		if (state.getValue(MjMachineBlock.ACTIVE) != active) {
			level.setBlock(pos, state.setValue(MjMachineBlock.ACTIVE, active), Block.UPDATE_ALL);
		}
	}

	private boolean canOutput(Recipe recipe) {
		ItemStack out = items.get(2);
		return out.isEmpty()
				|| (out.is(recipe.output()) && out.getCount() + recipe.outCount() <= out.getMaxStackSize());
	}

	private void produce(Recipe recipe) {
		ItemStack out = items.get(2);
		if (out.isEmpty()) {
			items.set(2, new ItemStack(recipe.output(), recipe.outCount()));
		} else {
			out.grow(recipe.outCount());
		}
	}

	// --- MjReceiver ---

	@Override
	public double receive(double packetEnergy, Direction face) {
		if (packetEnergy > MjPressure.LV.packetSize()) {
			return 0;
		}
		double accepted = Math.min(CAPACITY - energy, packetEnergy);
		if (accepted <= 0) {
			return 0;
		}
		energy += accepted;
		setChanged();
		return accepted;
	}

	// --- WorldlyContainer ---

	@Override
	public int getContainerSize() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
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
		ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
		if (!removed.isEmpty()) {
			setChanged();
		}
		return removed;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		if (stack.getCount() > getMaxStackSize()) {
			stack.setCount(getMaxStackSize());
		}
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		items.clear();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.DOWN ? new int[]{2} : new int[]{0, 1};
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return slot < 2;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return slot == 2;
	}

	// --- NBT ---

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
		output.putDouble("energy", energy);
		output.putInt("progress", progress);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ContainerHelper.loadAllItems(input, items);
		energy = input.getDoubleOr("energy", 0.0);
		progress = input.getIntOr("progress", 0);
	}
}
