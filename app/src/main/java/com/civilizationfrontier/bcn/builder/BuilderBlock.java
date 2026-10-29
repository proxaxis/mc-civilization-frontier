package com.civilizationfrontier.bcn.builder;

import java.util.List;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.builder.FillerBlock.FillerBlockEntity;
import com.civilizationfrontier.bcn.builder.LandmarkBlock.BlockVolume;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

/** ビルダー. blueprint/template を読み込み、インベントリ素材で実物を組み立てる (MJ 消費). */
public class BuilderBlock extends Block implements EntityBlock {

	private static final double MJ_PER_BLOCK = 1.0;

	public BuilderBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BuilderBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return type == MachineBlockEntities.BUILDER
				? (l, p, s, be) -> BuilderBlockEntity.tick(l, p, s, (BuilderBlockEntity) be)
				: null;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof BuilderBlockEntity bbe)) {
			return InteractionResult.SUCCESS;
		}
		if (stack.getItem() instanceof SnapshotItem) {
			if (bbe.getItem(0).isEmpty()) {
				bbe.setItem(0, stack.copyWithCount(1));
				stack.shrink(1);
				player.sendSystemMessage(Component.literal("Loaded blueprint"));
			} else {
				player.sendSystemMessage(Component.literal("Blueprint slot occupied"));
			}
			return InteractionResult.SUCCESS;
		}
		if (!bbe.getItem(0).isEmpty()) {
			ItemStack out = bbe.getItem(0).copy();
			bbe.setItem(0, ItemStack.EMPTY);
			if (!player.getInventory().add(out)) {
				net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), out);
			}
			player.sendSystemMessage(Component.literal("Ejected blueprint"));
		}
		return InteractionResult.SUCCESS;
	}

	public static class BuilderBlockEntity extends BlockEntity implements MjReceiver, WorldlyContainer {

		private static final int SLOTS = 28;
		private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		private double energy;
		private int cursor;

		public BuilderBlockEntity(BlockPos pos, BlockState state) {
			super(MachineBlockEntities.BUILDER, pos, state);
		}

		public static void tick(Level level, BlockPos pos, BlockState state, BuilderBlockEntity be) {
			if (!level.isClientSide() && level.getGameTime() % 4 == 0) {
				be.step(level, pos);
			}
		}

		private void step(Level level, BlockPos pos) {
			ItemStack snap = items.get(0);
			if (!(snap.getItem() instanceof SnapshotItem si) || SnapshotItem.read(snap) == null || energy < MJ_PER_BLOCK) {
				return;
			}
			CompoundTag data = SnapshotItem.read(snap);
			List<String> palette = si.palette(snap);
			ListTag cells = si.cells(snap);
			int[] size = si.size(snap);
			if (cells == null || size == null || cells.size() == 0) {
				return;
			}
			int total = size[0] * size[1] * size[2];
			BlockPos origin = buildOrigin(level, pos, size, data);
			int guard = 0;
			while (guard++ < 128 && cursor < total) {
				int i = cursor++;
				int x = i % size[0];
				int z = (i / size[0]) % size[2];
				int y = i / (size[0] * size[2]);
				BlockPos cell = origin.offset(x, y, z);
				if (!level.isLoaded(cell) || cell.equals(pos)) {
					continue;
				}
				int idx = cells.getInt(i).orElse(0);
				String name = palette.get(idx);
				if (name.equals("minecraft:air")) {
					continue;
				}
				if (!level.getBlockState(cell).canBeReplaced() && !level.getBlockState(cell).isAir()) {
					continue;
				}
				if (!consumeBlock(name)) {
					continue;
				}
				Block b = SnapshotItem.decode(name);
				if (b != null && b != net.minecraft.world.level.block.Blocks.AIR) {
					level.setBlock(cell, b.defaultBlockState(), Block.UPDATE_ALL);
					energy -= MJ_PER_BLOCK;
					setChanged();
					return;
				}
			}
			if (cursor >= total) {
				cursor = 0;
			}
		}

		private BlockPos buildOrigin(Level level, BlockPos pos, int[] size, CompoundTag data) {
			return data.getIntArray("origin").map(a -> new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ()))
					.orElse(pos.above());
		}

		private boolean consumeBlock(String name) {
			for (int i = 1; i < SLOTS; i++) {
				ItemStack s = items.get(i);
				if (!s.isEmpty() && s.getItem() instanceof BlockItem bi
						&& SnapshotItem.decode(name) == bi.getBlock()) {
					s.shrink(1);
					return true;
				}
			}
			return false;
		}

		@Override
		public double receive(double packetEnergy, Direction face) {
			double accepted = Math.min(128.0 - energy, packetEnergy);
			energy += Math.max(0, accepted);
			return Math.max(0, accepted);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			ContainerHelper.loadAllItems(input, items);
			energy = input.getDoubleOr("energy", 0);
		}

		@Override
		protected void saveAdditional(ValueOutput output) {
			super.saveAdditional(output);
			ContainerHelper.saveAllItems(output, items);
			output.putDouble("energy", energy);
		}

		@Override
		public int getContainerSize() { return SLOTS; }

		@Override
		public boolean isEmpty() {
			for (ItemStack s : items) if (!s.isEmpty()) return false;
			return true;
		}

		@Override
		public ItemStack getItem(int slot) { return items.get(slot); }

		@Override
		public ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(items, slot, amount); }

		@Override
		public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }

		@Override
		public void setItem(int slot, ItemStack stack) { items.set(slot, stack); }

		@Override
		public void clearContent() { items.clear(); }

		@Override
		public void setChanged() { super.setChanged(); }

		@Override
		public boolean stillValid(Player player) { return true; }

		@Override
		public int[] getSlotsForFace(Direction face) {
			int[] slots = new int[SLOTS - 1];
			for (int i = 0; i < slots.length; i++) slots[i] = i + 1;
			return slots;
		}

		@Override
		public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction face) {
			return slot > 0 && stack.getItem() instanceof BlockItem;
		}

		@Override
		public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {
			return false;
		}
	}
}
