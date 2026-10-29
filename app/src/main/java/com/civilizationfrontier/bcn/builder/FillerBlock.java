package com.civilizationfrontier.bcn.builder;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.builder.LandmarkBlock.BlockVolume;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;

/**
 * フィラー. landmark の直方体範囲にインベントリのブロックを充填する (MJ 消費).
 * 空の手で右クリックでパターン切替 (fill/clear/flatten/stairs).
 */
public class FillerBlock extends Block implements EntityBlock {

	public static final IntegerProperty PATTERN = IntegerProperty.create("pattern", 0, 3);
	// 0=fill, 1=clear, 2=flatten, 3=stairs(hollow shell)
	private static final String[] PATTERN_NAMES = {"fill", "clear", "flatten", "stairs"};
	private static final double MJ_PER_OP = 1.0;

	public FillerBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(PATTERN, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PATTERN);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FillerBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return type == MachineBlockEntities.FILLER
				? (l, p, s, be) -> FillerBlockEntity.tick(l, p, s, (FillerBlockEntity) be)
				: null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!level.isClientSide()) {
			int next = (state.getValue(PATTERN) + 1) % PATTERN_NAMES.length;
			level.setBlock(pos, state.setValue(PATTERN, next), Block.UPDATE_ALL);
			player.sendSystemMessage(Component.literal("Filler pattern: " + PATTERN_NAMES[next]));
		}
		return InteractionResult.SUCCESS;
	}

	public static class FillerBlockEntity extends BlockEntity implements MjReceiver, WorldlyContainer {

		private static final int SLOTS = 27;
		private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
		private double energy;
		private int cursor;
		private BlockVolume volume;

		public FillerBlockEntity(BlockPos pos, BlockState state) {
			super(MachineBlockEntities.FILLER, pos, state);
		}

		public static void tick(Level level, BlockPos pos, BlockState state, FillerBlockEntity be) {
			if (level.isClientSide()) {
				return;
			}
			if (level.getGameTime() % 4 != 0) {
				return;
			}
			be.step(level, pos, state);
		}

		private void step(Level level, BlockPos pos, BlockState state) {
			if (energy < MJ_PER_OP) {
				return;
			}
			if (volume == null || cursor == 0) {
				volume = findAdjacentVolume(level, pos);
				if (volume == null) {
					cursor = 0;
					return;
				}
			}
			int pattern = state.getValue(PATTERN);
			int guard = 0;
			while (guard++ < 64) {
				BlockPos cell = cellAt(cursor);
				cursor++;
				if (cell == null) {
					cursor = 0;
					return;
				}
				if (!volume.contains(cell) || !level.isLoaded(cell) || cell.equals(pos)) {
					continue;
				}
				BlockState cur = level.getBlockState(cell);
				boolean did;
				switch (pattern) {
					case 1 -> did = tryRemove(level, cell, cur);
					case 2 -> did = cell.getY() > volume.minY() ? tryRemove(level, cell, cur)
							: tryPlace(level, cell, cur);
					case 3 -> did = volume.onBoundary(cell) ? tryPlace(level, cell, cur)
							: tryRemove(level, cell, cur);
					default -> did = tryPlace(level, cell, cur);
				}
				if (did) {
					energy -= MJ_PER_OP;
					setChanged();
					return;
				}
			}
		}

		private BlockPos cellAt(int i) {
			int sx = volume.sizeX(), sy = volume.sizeY(), sz = volume.sizeZ();
			if (i >= sx * sy * sz) {
				return null;
			}
			int x = i % sx;
			int y = (i / sx) % sy;
			int z = i / (sx * sy);
			return new BlockPos(volume.minX() + x, volume.minY() + y, volume.minZ() + z);
		}

		private boolean tryPlace(Level level, BlockPos cell, BlockState cur) {
			if (!cur.isAir() && !cur.canBeReplaced()) {
				return false;
			}
			ItemStack stack = firstBlockStack();
			if (stack.isEmpty()) {
				return false;
			}
			level.setBlock(cell, ((BlockItem) stack.getItem()).getBlock().defaultBlockState(), Block.UPDATE_ALL);
			stack.shrink(1);
			return true;
		}

		private boolean tryRemove(Level level, BlockPos cell, BlockState cur) {
			if (cur.isAir() || cur.getBlock() instanceof LandmarkBlock) {
				return false;
			}
			level.destroyBlock(cell, true);
			return true;
		}

		private ItemStack firstBlockStack() {
			for (ItemStack s : items) {
				if (!s.isEmpty() && s.getItem() instanceof BlockItem) {
					return s;
				}
			}
			return ItemStack.EMPTY;
		}

		static BlockVolume findAdjacentVolume(Level level, BlockPos pos) {
			for (Direction dir : Direction.values()) {
				BlockPos np = pos.relative(dir);
				if (level.getBlockState(np).getBlock() instanceof LandmarkBlock lb && !lb.isPath()) {
					BlockVolume v = LandmarkBlock.findVolume(level, np);
					if (v != null) {
						return v;
					}
				}
			}
			return null;
		}

		@Override
		public double receive(double packetEnergy, Direction face) {
			double accepted = Math.min(64.0 - energy, packetEnergy);
			energy += Math.max(0, accepted);
			return Math.max(0, accepted);
		}

		@Override
		protected void loadAdditional(ValueInput input) {
			super.loadAdditional(input);
			ContainerHelper.loadAllItems(input, items);
			energy = input.getDoubleOr("energy", 0);
			cursor = input.getIntOr("cursor", 0);
		}

		@Override
		protected void saveAdditional(ValueOutput output) {
			super.saveAdditional(output);
			ContainerHelper.saveAllItems(output, items);
			output.putDouble("energy", energy);
			output.putInt("cursor", cursor);
		}

		@Override
		public int getContainerSize() {
			return SLOTS;
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
		}

		@Override
		public void clearContent() {
			items.clear();
		}

		@Override
		public void setChanged() {
			super.setChanged();
		}

		@Override
		public boolean stillValid(Player player) {
			return true;
		}

		@Override
		public int[] getSlotsForFace(Direction face) {
			int[] slots = new int[SLOTS];
			for (int i = 0; i < SLOTS; i++) {
				slots[i] = i;
			}
			return slots;
		}

		@Override
		public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction face) {
			return stack.getItem() instanceof BlockItem;
		}

		@Override
		public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {
			return false;
		}
	}
}
