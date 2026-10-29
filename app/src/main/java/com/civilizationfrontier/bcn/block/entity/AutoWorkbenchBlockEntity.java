package com.civilizationfrontier.bcn.block.entity;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 全自動作業台. グリッド (0-8) に並んだ素材のレシピを定期的に自動クラフトし、結果を 9 番スロットへ出力. */
public class AutoWorkbenchBlockEntity extends BlockEntity implements WorldlyContainer {

	public static final int GRID = 9;
	public static final int OUT = 9;
	private static final int INTERVAL = 20;
	private static final int[] IN_SLOTS = {0,1,2,3,4,5,6,7,8};
	private static final int[] OUT_SLOTS = {OUT};

	private final NonNullList<ItemStack> items = NonNullList.withSize(OUT + 1, ItemStack.EMPTY);
	private int cooldown;

	public AutoWorkbenchBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.AUTO_WORKBENCH, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AutoWorkbenchBlockEntity be) {
		if (level.isClientSide() || --be.cooldown > 0) {
			return;
		}
		be.cooldown = INTERVAL;
		be.craft(level);
	}

	private void craft(Level level) {
		List<ItemStack> grid = items.subList(0, GRID);
		CraftingInput input = CraftingInput.of(3, 3, List.copyOf(grid));
		if (input.isEmpty()) {
			return;
		}
		Optional<RecipeHolder<CraftingRecipe>> found =
				level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
		if (found.isEmpty()) {
			return;
		}
		CraftingRecipe recipe = found.get().value();
		ItemStack result = recipe.assemble(input);
		if (result.isEmpty()) {
			return;
		}
		ItemStack out = items.get(OUT);
		if (!out.isEmpty() && (!ItemStack.isSameItemSameComponents(out, result)
				|| out.getCount() + result.getCount() > out.getMaxStackSize())) {
			return;
		}
		List<ItemStack> remaining = recipe.getRemainingItems(input);
		for (int i = 0; i < GRID; i++) {
			ItemStack slot = items.get(i);
			if (slot.isEmpty()) {
				continue;
			}
			ItemStack rem = i < remaining.size() ? remaining.get(i) : ItemStack.EMPTY;
			slot.shrink(1);
			if (!rem.isEmpty() && slot.isEmpty()) {
				items.set(i, rem);
			}
		}
		if (out.isEmpty()) {
			items.set(OUT, result);
		} else {
			out.grow(result.getCount());
		}
		setChanged();
	}

	@Override public int getContainerSize() { return items.size(); }
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
	@Override public int[] getSlotsForFace(Direction side) { return side == Direction.DOWN ? OUT_SLOTS : IN_SLOTS; }
	@Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) { return slot < GRID && dir != Direction.DOWN; }
	@Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) { return slot == OUT; }

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
