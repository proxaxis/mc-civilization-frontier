package com.civilizationfrontier.bcn.block.entity;

import java.util.List;
import java.util.stream.IntStream;

import com.civilizationfrontier.bcn.block.BcnPlanterBlock;
import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 自動栽培機. 自身を中心とした 5x5 (自ブロックを除く 24 区画) の作物層を走査し、
 * 成熟した作物の収穫と空き耕地への種まきを自動で行う.
 *
 * - 稼働には MJ が必要. {@link MjReceiver} を実装し、ケーブル網 ({@link com.civilizationfrontier.bcn.energy.MjNetwork})
 *   から届くパケットを内部バッファに蓄える. 許容圧は MV (51 MJ-P) まで.
 * - 種スロット 0-8, 収穫スロット 9-17 の 18 スロット. WorldlyContainer 実装で
 *   ホッパー等からの種投入・収穫物引き出しが可能.
 * - 収穫物は隣接する Container (チェスト・パイプ BE 等) へ自動放出.
 * - 稼働中は Generator と同率で EFP を加算.
 */
public class PlanterBlockEntity extends BlockEntity implements WorldlyContainer, MjReceiver {

	public static final double MJ_CAPACITY = 1000;
	public static final double MJ_PER_PLANT = 10;
	public static final double MJ_PER_HARVEST = 20;
	public static final int WORK_INTERVAL = 10;
	// エンジン系と同率 (2.8e-6/tick) を 10 tick 間隔の稼働へ換算
	public static final double EFP_PER_ACTION = 2.8e-5;

	public static final int SEED_SLOTS = 9;
	public static final int OUTPUT_SLOTS = 9;

	// 5x5 (中心の機械自身を除く 24 区画), 作物層は機械と同じ Y
	private static final int[][] PLOTS = buildPlots();
	private static final int[] TOP_SLOTS = IntStream.range(0, SEED_SLOTS).toArray();
	private static final int[] BOTTOM_SLOTS = IntStream.range(SEED_SLOTS, SEED_SLOTS + OUTPUT_SLOTS).toArray();
	private static final int[] SIDE_SLOTS = IntStream.range(0, SEED_SLOTS + OUTPUT_SLOTS).toArray();

	private final NonNullList<ItemStack> items = NonNullList.withSize(SEED_SLOTS + OUTPUT_SLOTS, ItemStack.EMPTY);

	private double mj;
	private double efpBuffer;
	private int scanIndex;
	private int cooldown;

	public PlanterBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.PLANTER, pos, state);
	}

	private static int[][] buildPlots() {
		int[][] plots = new int[24][2];
		int i = 0;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				if (dx == 0 && dz == 0) continue;
				plots[i++] = new int[]{dx, dz};
			}
		}
		return plots;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, PlanterBlockEntity be) {
		if (level.isClientSide() || !(level instanceof ServerLevel server)) return;

		if (++be.cooldown >= WORK_INTERVAL) {
			be.cooldown = 0;
			boolean active = be.work(server, pos);
			be.eject(server, pos);
			if (state.getValue(BcnPlanterBlock.ACTIVE) != active) {
				level.setBlock(pos, state.setValue(BcnPlanterBlock.ACTIVE, active), 3);
			}
		}
	}

	/** 24 区画を 1 区画ずつ巡回し、最初に見つかった作業 (収穫 or 種まき) を 1 回だけ実行する. */
	private boolean work(ServerLevel level, BlockPos pos) {
		for (int i = 0; i < PLOTS.length; i++) {
			int[] d = PLOTS[scanIndex];
			scanIndex = (scanIndex + 1) % PLOTS.length;
			BlockPos cropPos = pos.offset(d[0], 0, d[1]);
			BlockState crop = level.getBlockState(cropPos);

			if (isMature(crop)) {
				if (mj < MJ_PER_HARVEST) return false;
				harvest(level, cropPos, crop);
				mj -= MJ_PER_HARVEST;
				addEfp(level);
				setChanged();
				return true;
			}
			if (crop.canBeReplaced()) {
				int seedSlot = findSeed(level, cropPos);
				if (seedSlot >= 0) {
					if (mj < MJ_PER_PLANT) return false;
					BlockState seedState = plantableState(items.get(seedSlot));
					items.get(seedSlot).shrink(1);
					level.setBlock(cropPos, seedState, 3);
					mj -= MJ_PER_PLANT;
					addEfp(level);
					setChanged();
					return true;
				}
			}
		}
		return false;
	}

	private void addEfp(ServerLevel level) {
		efpBuffer += EFP_PER_ACTION;
		if (efpBuffer >= 1.0) {
			long whole = (long) efpBuffer;
			efpBuffer -= whole;
			BcnEfp.add(level.getServer(), whole);
		}
	}

	/** 収穫. ドロップのうち種になりうるものは種スロットへ優先格納し、残りを収穫スロットへ. */
	private void harvest(ServerLevel level, BlockPos cropPos, BlockState crop) {
		List<ItemStack> drops = Block.getDrops(crop, level, cropPos, level.getBlockEntity(cropPos));
		level.destroyBlock(cropPos, false);
		for (ItemStack drop : drops) {
			ItemStack rem = drop.copy();
			if (plantableState(rem) != null) {
				rem = insertInto(0, SEED_SLOTS, rem);
			}
			if (!rem.isEmpty()) rem = insertInto(SEED_SLOTS, items.size(), rem);
			if (!rem.isEmpty()) Block.popResource(level, cropPos, rem);
		}
	}

	/** 収穫スロットの内容を隣接コンテナ (チェスト・ホッパー・パイプBE等) へ押し出す. */
	private void eject(ServerLevel level, BlockPos pos) {
		for (int slot = SEED_SLOTS; slot < items.size(); slot++) {
			ItemStack stack = items.get(slot);
			if (stack.isEmpty()) continue;
			for (Direction dir : Direction.values()) {
				if (stack.isEmpty()) break;
				Container target = HopperBlockEntity.getContainerAt(level, pos.relative(dir));
				if (target == null) continue;
				stack = HopperBlockEntity.addItem(this, target, stack, dir.getOpposite());
				items.set(slot, stack);
			}
		}
		setChanged();
	}

	private ItemStack insertInto(int from, int to, ItemStack stack) {
		for (int i = from; i < to && !stack.isEmpty(); i++) {
			ItemStack cur = items.get(i);
			if (!cur.isEmpty() && ItemStack.isSameItemSameComponents(cur, stack)) {
				int space = Math.min(cur.getMaxStackSize(), getMaxStackSize()) - cur.getCount();
				if (space > 0) {
					int n = Math.min(space, stack.getCount());
					cur.grow(n);
					stack.shrink(n);
				}
			}
		}
		for (int i = from; i < to && !stack.isEmpty(); i++) {
			if (items.get(i).isEmpty()) {
				items.set(i, stack.copy());
				stack.setCount(0);
			}
		}
		return stack;
	}

	/** 種スロットから cropPos に植えられる種を探し、スロット番号を返す. */
	private int findSeed(ServerLevel level, BlockPos cropPos) {
		for (int i = 0; i < SEED_SLOTS; i++) {
			ItemStack stack = items.get(i);
			BlockState seedState = plantableState(stack);
			if (seedState != null && seedState.canSurvive(level, cropPos)) {
				return i;
			}
		}
		return -1;
	}

	/** インタラクション用: 手持ち 1 個を種スロットへ挿入する. */
	public boolean insertSeed(ItemStack stack) {
		if (plantableState(stack) == null) return false;
		ItemStack rem = insertInto(0, SEED_SLOTS, stack.copyWithCount(1));
		if (rem.isEmpty()) {
			stack.shrink(1);
			setChanged();
			return true;
		}
		return false;
	}

	/** stack が植え付け可能な作物アイテムならその設置 BlockState を返す. */
	private static BlockState plantableState(ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) return null;
		Block block = blockItem.getBlock();
		if (block instanceof CropBlock || block instanceof NetherWartBlock) {
			return block.defaultBlockState();
		}
		if (block instanceof BonemealableBlock && ageProperty(block.defaultBlockState()) != null) {
			return block.defaultBlockState();
		}
		return null;
	}

	private static boolean isMature(BlockState state) {
		Block block = state.getBlock();
		if (block instanceof CropBlock crop) return crop.isMaxAge(state);
		if (block instanceof NetherWartBlock) return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
		if (block instanceof BonemealableBlock) {
			IntegerProperty age = ageProperty(state);
			return age != null && state.getValue(age) >= maxAge(age);
		}
		return false;
	}

	private static IntegerProperty ageProperty(BlockState state) {
		for (Property<?> property : state.getProperties()) {
			if ("age".equals(property.getName()) && property instanceof IntegerProperty intProp) {
				return intProp;
			}
		}
		return null;
	}

	private static int maxAge(IntegerProperty property) {
		List<Integer> values = property.getPossibleValues();
		return values.get(values.size() - 1);
	}

	public double mjStored() {
		return mj;
	}

	public int seedCount() {
		int n = 0;
		for (int i = 0; i < SEED_SLOTS; i++) n += items.get(i).getCount();
		return n;
	}

	public int outputCount() {
		int n = 0;
		for (int i = SEED_SLOTS; i < items.size(); i++) n += items.get(i).getCount();
		return n;
	}

	// --- MjReceiver ---

	@Override
	public double receive(double packetEnergy, Direction face) {
		if (packetEnergy <= 0 || packetEnergy > MjPressure.MV.packetSize()) return 0;
		double accepted = Math.min(MJ_CAPACITY - mj, packetEnergy);
		if (accepted > 0) {
			mj += accepted;
			setChanged();
		}
		return accepted;
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
		return slot < SEED_SLOTS && plantableState(stack) != null;
	}

	// --- WorldlyContainer ---

	@Override
	public int[] getSlotsForFace(Direction side) {
		return switch (side) {
			case UP -> TOP_SLOTS;
			case DOWN -> BOTTOM_SLOTS;
			default -> SIDE_SLOTS;
		};
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return slot < SEED_SLOTS && plantableState(stack) != null;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return slot >= SEED_SLOTS;
	}

	// --- BlockEntity ---

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("mj", mj);
		output.putDouble("efp_buffer", efpBuffer);
		output.putInt("scan_index", scanIndex);
		ContainerHelper.saveAllItems(output, items);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		mj = input.getDoubleOr("mj", 0);
		efpBuffer = input.getDoubleOr("efp_buffer", 0);
		scanIndex = input.getIntOr("scan_index", 0);
		ContainerHelper.loadAllItems(input, items);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null) Containers.dropContents(level, pos, this);
	}
}
