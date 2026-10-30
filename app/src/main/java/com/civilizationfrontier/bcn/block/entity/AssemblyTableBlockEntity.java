package com.civilizationfrontier.bcn.block.entity;

import java.util.List;

import com.civilizationfrontier.bcn.energy.BcnProcessingRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 精密作業台. レーザーから供給されるエネルギーを蓄え、投入された素材が
 * processing レシピ ("assembly_table") に一致した時点で製作を実行する.
 * BC の assembly table 準拠: 自前の MJ 入力は持たず、レーザーが唯一の動力.
 */
public class AssemblyTableBlockEntity extends BlockEntity {

	public static final double CAPACITY = 10000.0;
	private static final int SLOTS = 12;

	private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
	private double energy;
	private double craftedTotal;

	public AssemblyTableBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.ASSEMBLY_TABLE, pos, state);
	}

	/** レーザーからの受電. 消費可能量を超える分は残る. */
	public void addEnergy(double amount) {
		energy = Math.min(CAPACITY, energy + amount);
		setChanged();
	}

	public double energy() {
		return energy;
	}

	public List<ItemStack> items() {
		return items;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AssemblyTableBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		BcnProcessingRecipes.Recipe recipe = BcnProcessingRecipes.find("assembly_table", be.items);
		if (recipe == null) {
			return;
		}
		double cost = recipe.ticks() * recipe.mjPerTick();
		if (be.energy < cost) {
			return;
		}
		be.energy -= cost;
		for (BcnProcessingRecipes.Ingredient in : recipe.inputs()) {
			be.consume(in.item(), in.count());
		}
		ItemStack out = new ItemStack(recipe.output(), recipe.outCount());
		be.eject(level, pos, out);
		be.craftedTotal++;
		be.setChanged();
	}

	private void consume(net.minecraft.world.item.Item item, int count) {
		int remaining = count;
		for (int i = 0; i < items.size() && remaining > 0; i++) {
			ItemStack slot = items.get(i);
			if (slot.is(item)) {
				int take = Math.min(remaining, slot.getCount());
				slot.shrink(take);
				remaining -= take;
			}
		}
	}

	private void eject(Level level, BlockPos pos, ItemStack stack) {
		for (Direction d : Direction.values()) {
			BlockPos np = pos.relative(d);
			if (level.getBlockEntity(np) instanceof net.minecraft.world.Container c) {
				insertInto(c, stack);
				if (stack.isEmpty()) {
					return;
				}
			}
		}
		if (!stack.isEmpty()) {
			Block.popResource(level, pos.above(), stack);
		}
	}

	private static void insertInto(net.minecraft.world.Container c, ItemStack stack) {
		for (int i = 0; i < c.getContainerSize() && !stack.isEmpty(); i++) {
			ItemStack slot = c.getItem(i);
			if (slot.isEmpty()) {
				c.setItem(i, stack.copy());
				stack.setCount(0);
			} else if (ItemStack.isSameItemSameComponents(slot, stack)
					&& slot.getCount() < slot.getMaxStackSize()) {
				int move = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
				slot.grow(move);
				stack.shrink(move);
				c.setItem(i, slot);
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("energy", energy);
		output.putDouble("crafted_total", craftedTotal);
		ContainerHelper.saveAllItems(output, items);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		energy = input.getDoubleOr("energy", 0);
		craftedTotal = input.getDoubleOr("crafted_total", 0);
		ContainerHelper.loadAllItems(input, items);
	}
}
