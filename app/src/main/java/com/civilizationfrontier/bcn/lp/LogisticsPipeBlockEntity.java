package com.civilizationfrontier.bcn.lp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.civilizationfrontier.bcn.block.entity.ItemPipeBlockEntity;
import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import com.civilizationfrontier.bcn.lp.LogisticsNetwork.FaceContainer;
import com.civilizationfrontier.bcn.lp.LogisticsNetwork.Sink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 物流パイプの BlockEntity. 種別 (LpKind) はブロック側が持ち、
 * 本クラスが要求/供給/補給/加工/モジュールの全動作と MJ 消費を担う.
 *
 * NBT 設定で操作可能 (GUI 未実装のため検証は data modify で行う):
 * - {@code filter}: 基本=回収フィルタ / 要求=発注残 (ItemStack) / 補給=維持アイテム
 * - {@code modules}: chassis のモジュール列 (ItemStack)
 * - {@code power}: MJ バッファ
 */
public class LogisticsPipeBlockEntity extends ItemPipeBlockEntity implements MjReceiver {

	public static final double POWER_CAPACITY = 128;
	/** 発送1個あたりの MJ 消費 (要求側が払う). */
	public static final double COST_PER_ITEM = 2;
	/** クラフト1回あたりの MJ 消費 (加工パイプが払う). */
	public static final double COST_PER_CRAFT = 16;
	private static final int REQUEST_INTERVAL = 10;
	private static final int SUPPLY_INTERVAL = 40;
	private static final int MODULE_INTERVAL = 20;
	private static final int CRAFT_INTERVAL = 20;

	/** 回収フィルタ / 発注残 / 補給指定. */
	private ItemStack filter = ItemStack.EMPTY;
	/** 宛先が本パイプで未到着の発送済み個数. */
	private int inflight;
	private double power;
	private final NonNullList<ItemStack> modules;
	/** 加工用の材料バッファ (内部配送の受け皿). */
	private final List<ItemStack> craftBuffer = new ArrayList<>();
	private ItemStack craftOut = ItemStack.EMPTY;
	private int craftPending;
	private long craftDest = Transit.NO_DEST;
	private int craftDestFace = LogisticsNetwork.FACE_EJECT;
	private int craftInflight;
	private int requestCd;
	private int supplyCd;
	private int moduleCd;
	private int craftCd;

	public LogisticsPipeBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.LOGISTICS_PIPE, pos, state);
		modules = NonNullList.withSize(kind().moduleSlots(), ItemStack.EMPTY);
	}

	public LpKind kind() {
		return ((LogisticsPipeBlock) getBlockState().getBlock()).kind();
	}

	public ItemStack filter() {
		return filter;
	}

	/** フィルタ/発注の書き換え (ブロックの右クリック操作・NBT 経由). */
	public void setFilter(ItemStack stack) {
		filter = stack == null ? ItemStack.EMPTY : stack.copy();
		inflight = 0;
		setChanged();
	}

	public List<ItemStack> modules() {
		return modules;
	}

	public double power() {
		return power;
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

	private void spend(double mj) {
		power = Math.max(0, power - mj);
	}

	private boolean powered() {
		return power >= COST_PER_ITEM;
	}

	// ---- tick ----

	@Override
	public void tick(Level level, BlockPos pos) {
		super.tick(level, pos);
		if (--requestCd <= 0) {
			requestCd = REQUEST_INTERVAL;
			if (kind() == LpKind.REQUEST) {
				tickRequest(level, pos);
			}
		}
		if (--supplyCd <= 0) {
			supplyCd = SUPPLY_INTERVAL;
			if (kind() == LpKind.SUPPLIER) {
				tickSupply(level, pos, filter);
			}
		}
		if (--craftCd <= 0) {
			craftCd = CRAFT_INTERVAL;
			if (craftingCapable()) {
				tickCraft(level, pos);
			}
		}
		if (kind().isChassis() && --moduleCd <= 0) {
			moduleCd = MODULE_INTERVAL;
			tickModules(level, pos);
		}
	}

	/** 無宛先アイテムへシンクを割り当ててから親クラスの出口決定へ渡す. */
	@Override
	protected Direction chooseExit(Level level, BlockPos pos, Transit t) {
		if (!t.hasRoute() && !t.stack.isEmpty()) {
			Sink sink = LogisticsNetwork.resolveSink(level, pos, t.stack);
			if (sink != null) {
				List<Direction> route = LogisticsNetwork.route(level, pos, sink.pipe());
				if (route != null) {
					t.dest = sink.pipe().asLong();
					t.destFace = sink.face();
					t.route = route.toArray(new Direction[0]);
					t.routeIdx = 0;
				}
			}
		}
		return super.chooseExit(level, pos, t);
	}

	// ---- 宛先解決 (シンク側) ----

	/**
	 * このパイプが stack の受け皿になるなら Sink を返す.
	 * 回収系 (基本/シンク系モジュール) のみがシンクになる.
	 */
	public Sink sinkFor(ItemStack stack) {
		FaceContainer target = LogisticsNetwork.attachedContainer(level, worldPosition);
		if (target == null) {
			return null;
		}
		int priority = switch (kind()) {
		case BASIC -> filter.isEmpty() ? 1 : ItemStack.isSameItemSameComponents(filter, stack) ? 30 : 0;
		default -> 0;
		};
		if (kind().isChassis()) {
			priority = Math.max(priority, chassisSinkPriority(stack, target.container()));
		}
		return priority > 0 ? new Sink(worldPosition, target.face().ordinal(), priority) : null;
	}

	private int chassisSinkPriority(ItemStack stack, Container container) {
		int best = 0;
		for (ItemStack m : modules) {
			LpModule mod = LpModuleItem.kindOf(m);
			if (mod == null) {
				continue;
			}
			ItemStack f = LpModuleItem.filterOf(m);
			int pri = switch (mod) {
			case ITEM_SINK -> !f.isEmpty() && ItemStack.isSameItemSameComponents(f, stack) ? 30 : 0;
			case POLYMORPHIC_SINK -> containsItem(container, stack) ? 25 : 0;
			case TERMINUS_SINK -> !f.isEmpty() && ItemStack.isSameItemSameComponents(f, stack) ? 10 : 0;
			case PASSIVE_SUPPLIER -> !f.isEmpty() && ItemStack.isSameItemSameComponents(f, stack)
					&& countIn(container, f) < f.getCount() ? 35 : 0;
			default -> 0;
			};
			best = Math.max(best, pri);
		}
		return best;
	}

	// ---- 供給側 (プロバイダー) ----

	/** プロバイダーとして扱う隣接コンテナ. 非プロバイダーなら null. */
	public FaceContainer providerSource() {
		boolean provider = kind() == LpKind.PROVIDER
				|| kind().isChassis() && hasModule(LpModule.PROVIDER);
		return provider ? LogisticsNetwork.attachedContainer(level, worldPosition) : null;
	}

	private boolean hasModule(LpModule mod) {
		for (ItemStack m : modules) {
			if (LpModuleItem.kindOf(m) == mod) {
				return true;
			}
		}
		return false;
	}

	// ---- 要求側 ----

	private void tickRequest(Level level, BlockPos pos) {
		if (filter.isEmpty() || !powered()) {
			return;
		}
		int want = Math.min(filter.getCount() - inflight, 16);
		if (want <= 0) {
			return;
		}
		FaceContainer attached = LogisticsNetwork.attachedContainer(level, pos);
		int face = attached != null ? attached.face().ordinal() : LogisticsNetwork.FACE_EJECT;
		int sent = LogisticsNetwork.request(level, filter.copyWithCount(want), pos, pos, face);
		if (sent > 0) {
			inflight += sent;
			spend(sent * COST_PER_ITEM);
			setChanged();
		}
	}

	private void tickSupply(Level level, BlockPos pos, ItemStack want) {
		if (want.isEmpty() || !powered()) {
			return;
		}
		FaceContainer target = LogisticsNetwork.attachedContainer(level, pos);
		if (target == null) {
			return;
		}
		int deficit = want.getCount() - countIn(target.container(), want) - inflight;
		if (deficit <= 0) {
			return;
		}
		int sent = LogisticsNetwork.request(level, want.copyWithCount(deficit), pos, pos,
				target.face().ordinal());
		if (sent > 0) {
			inflight += sent;
			spend(sent * COST_PER_ITEM);
			setChanged();
		}
	}

	// ---- 加工 ----

	private boolean craftingCapable() {
		return kind() == LpKind.CRAFTING || kind().isChassis() && hasModule(LpModule.CRAFTING);
	}

	/** 隣接バニラ Crafter のグリッド内容からクラフトレシピを解決する. */
	private Optional<RecipeHolder<CraftingRecipe>> crafterRecipe() {
		for (Direction d : Direction.values()) {
			if (level.getBlockEntity(worldPosition.relative(d)) instanceof CrafterBlockEntity crafter) {
				CraftingInput input = crafter.asCraftInput();
				if (input.isEmpty()) {
					continue;
				}
				return level.getServer().getRecipeManager()
						.getRecipeFor(RecipeType.CRAFTING, input, level);
			}
		}
		return Optional.empty();
	}

	private ItemStack craftResult(RecipeHolder<CraftingRecipe> recipe) {
		for (Direction d : Direction.values()) {
			if (level.getBlockEntity(worldPosition.relative(d)) instanceof CrafterBlockEntity crafter) {
				return recipe.value().assemble(crafter.asCraftInput());
			}
		}
		return ItemStack.EMPTY;
	}

	private Direction crafterFace() {
		for (Direction d : Direction.values()) {
			if (level.getBlockEntity(worldPosition.relative(d)) instanceof CrafterBlockEntity) {
				return d;
			}
		}
		return Direction.UP;
	}

	/** 要求品をクラフト対象として受け付ける. 受け付けたら true. */
	public boolean tryEnqueueCraft(ItemStack want, BlockPos dest, int destFace) {
		if (!craftingCapable() || craftPending > 0 || want.isEmpty()) {
			return false;
		}
		Optional<RecipeHolder<CraftingRecipe>> recipe = crafterRecipe();
		if (recipe.isEmpty()) {
			return false;
		}
		ItemStack result = craftResult(recipe.get());
		if (!ItemStack.isSameItem(result, want)) {
			return false;
		}
		craftOut = result.copyWithCount(1);
		craftPending = want.getCount();
		craftDest = dest.asLong();
		craftDestFace = destFace;
		craftBuffer.clear();
		craftInflight = 0;
		setChanged();
		return true;
	}

	private void tickCraft(Level level, BlockPos pos) {
		if (craftPending <= 0 || craftOut.isEmpty()) {
			return;
		}
		Optional<RecipeHolder<CraftingRecipe>> recipe = crafterRecipe();
		if (recipe.isEmpty() || !powered()) {
			return;
		}
		List<ItemStack> needed = ingredientsOf(recipe.get());
		if (craftInflight <= 0 && !hasAll(craftBuffer, needed)) {
			for (ItemStack n : needed) {
				int deficit = n.getCount() - countIn(craftBuffer, n);
				if (deficit <= 0) {
					continue;
				}
				int sent = LogisticsNetwork.request(level, n.copyWithCount(deficit), pos, pos,
						LogisticsNetwork.FACE_INTERNAL);
				if (sent > 0) {
					craftInflight += sent;
					inflight += sent;
					spend(sent * COST_PER_ITEM);
				}
			}
			setChanged();
		}
		if (craftInflight <= 0 && hasAll(craftBuffer, needed) && power >= COST_PER_CRAFT) {
			consumeAll(craftBuffer, needed);
			ItemStack result = craftOut.copyWithCount(Math.min(craftPending, resultCount(recipe.get())));
			List<Direction> route = LogisticsNetwork.route(level, pos, BlockPos.of(craftDest));
			if (route == null) {
				craftPending = 0;
				return;
			}
			addTransit(result, crafterFace(), craftDest, craftDestFace, route);
			craftPending -= result.getCount();
			spend(COST_PER_CRAFT);
			if (craftPending <= 0) {
				craftOut = ItemStack.EMPTY;
			}
			setChanged();
		}
	}

	private List<ItemStack> ingredientsOf(RecipeHolder<CraftingRecipe> recipe) {
		List<ItemStack> out = new ArrayList<>();
		for (Ingredient ing : recipe.value().placementInfo().ingredients()) {
			if (ing.isEmpty()) {
				continue;
			}
			Optional<ItemStack> first = ing.items().map(ItemStack::new).findFirst();
			if (first.isEmpty()) {
				continue;
			}
			ItemStack rep = first.get().copyWithCount(1);
			boolean merged = false;
			for (ItemStack o : out) {
				if (ItemStack.isSameItemSameComponents(o, rep)) {
					o.grow(1);
					merged = true;
					break;
				}
			}
			if (!merged) {
				out.add(rep);
			}
		}
		return out;
	}

	private int resultCount(RecipeHolder<CraftingRecipe> recipe) {
		return Math.max(1, craftResult(recipe).getCount());
	}

	private static boolean hasAll(List<ItemStack> buffer, List<ItemStack> needed) {
		for (ItemStack n : needed) {
			if (countIn(buffer, n) < n.getCount()) {
				return false;
			}
		}
		return true;
	}

	private static void consumeAll(List<ItemStack> buffer, List<ItemStack> needed) {
		for (ItemStack n : needed) {
			int remaining = n.getCount();
			for (int i = buffer.size() - 1; i >= 0 && remaining > 0; i--) {
				ItemStack b = buffer.get(i);
				if (!ItemStack.isSameItemSameComponents(b, n)) {
					continue;
				}
				int take = Math.min(b.getCount(), remaining);
				b.shrink(take);
				remaining -= take;
				if (b.isEmpty()) {
					buffer.remove(i);
				}
			}
		}
	}

	// ---- モジュール (chassis 能動系) ----

	private void tickModules(Level level, BlockPos pos) {
		FaceContainer inv = LogisticsNetwork.attachedContainer(level, pos);
		if (inv == null) {
			return;
		}
		for (ItemStack m : modules) {
			LpModule mod = LpModuleItem.kindOf(m);
			if (mod == null || !powered()) {
				continue;
			}
			switch (mod) {
			case EXTRACTOR -> extractOne(level, pos, inv);
			case QUICK_SORT -> quickSort(level, pos, inv);
			case ACTIVE_SUPPLIER -> tickSupply(level, pos, LpModuleItem.filterOf(m));
			default -> {
			}
			}
		}
	}

	private void extractOne(Level level, BlockPos pos, FaceContainer inv) {
		Container container = inv.container();
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			ItemStack in = container.getItem(slot);
			if (in.isEmpty()) {
				continue;
			}
			ItemStack taken = in.copyWithCount(1);
			if (!addTransit(taken, inv.face())) {
				return;
			}
			in.shrink(1);
			container.setItem(slot, in);
			spend(COST_PER_ITEM);
			setChanged();
			return;
		}
	}

	private void quickSort(Level level, BlockPos pos, FaceContainer inv) {
		Container container = inv.container();
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			ItemStack in = container.getItem(slot);
			if (in.isEmpty()) {
				continue;
			}
			Sink sink = LogisticsNetwork.resolveSink(level, pos, in);
			// Default Route (priority 1) と自分自身は仕分け対象外
			if (sink == null || sink.priority() <= 1 || sink.pipe().equals(pos)) {
				continue;
			}
			List<Direction> route = LogisticsNetwork.route(level, pos, sink.pipe());
			if (route == null) {
				continue;
			}
			ItemStack taken = container.removeItem(slot, in.getCount());
			if (addTransit(taken, inv.face(), sink.pipe().asLong(), sink.face(), route)) {
				spend(taken.getCount() * COST_PER_ITEM);
				setChanged();
				return;
			}
			container.setItem(slot, taken);
			return;
		}
	}

	/** モジュール挿入. 空きスロットがあれば 1 個受け入れて true. */
	public boolean insertModule(ItemStack held) {
		if (!kind().isChassis() || held.isEmpty()) {
			return false;
		}
		for (int i = 0; i < modules.size(); i++) {
			if (modules.get(i).isEmpty()) {
				modules.set(i, held.copyWithCount(1));
				setChanged();
				return true;
			}
		}
		return false;
	}

	/** 末尾モジュールを取り外して返す (無ければ EMPTY). */
	public ItemStack popModule() {
		for (int i = modules.size() - 1; i >= 0; i--) {
			if (!modules.get(i).isEmpty()) {
				ItemStack out = modules.get(i);
				modules.set(i, ItemStack.EMPTY);
				setChanged();
				return out;
			}
		}
		return ItemStack.EMPTY;
	}

	// ---- 到達通知 ----

	@Override
	protected void onDeliveryArrived(ItemStack stack) {
		inflight = Math.max(0, inflight - stack.getCount());
		craftInflight = Math.max(0, craftInflight - stack.getCount());
		if (kind() == LpKind.REQUEST && !filter.isEmpty()
				&& ItemStack.isSameItemSameComponents(filter, stack)) {
			filter.shrink(stack.getCount());
			if (filter.getCount() <= 0) {
				filter = ItemStack.EMPTY;
			}
		}
		setChanged();
	}

	/** 内部配送 (destFace=FACE_INTERNAL): 加工材料バッファへ入れる. */
	@Override
	protected ItemStack internalDelivery(ItemStack stack) {
		if (craftPending <= 0) {
			return stack;
		}
		craftBuffer.add(stack.copy());
		setChanged();
		return ItemStack.EMPTY;
	}

	// ---- コンテナ集計 ----

	private static int countIn(Container container, ItemStack match) {
		int n = 0;
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack s = container.getItem(i);
			if (ItemStack.isSameItemSameComponents(s, match)) {
				n += s.getCount();
			}
		}
		return n;
	}

	private static int countIn(List<ItemStack> stacks, ItemStack match) {
		int n = 0;
		for (ItemStack s : stacks) {
			if (ItemStack.isSameItemSameComponents(s, match)) {
				n += s.getCount();
			}
		}
		return n;
	}

	private static boolean containsItem(Container container, ItemStack match) {
		for (int i = 0; i < container.getContainerSize(); i++) {
			if (ItemStack.isSameItem(container.getItem(i), match)) {
				return true;
			}
		}
		return false;
	}

	// ---- NBT ----

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		out.putDouble("power", power);
		out.putInt("fly", inflight);
		if (!filter.isEmpty()) {
			out.store("flt", ItemStack.CODEC, filter);
		}
		if (kind().isChassis()) {
			ValueOutput.ValueOutputList list = out.childrenList("modules");
			for (ItemStack m : modules) {
				list.addChild().store("s", ItemStack.OPTIONAL_CODEC, m);
			}
		}
		if (craftPending > 0) {
			out.store("co", ItemStack.CODEC, craftOut);
			out.putInt("cp", craftPending);
			out.putLong("cd", craftDest);
			out.putInt("cf", craftDestFace);
			out.putInt("ci", craftInflight);
			ValueOutput.ValueOutputList buf = out.childrenList("buf");
			for (ItemStack b : craftBuffer) {
				buf.addChild().store("s", ItemStack.OPTIONAL_CODEC, b);
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		power = in.getDoubleOr("power", 0);
		inflight = in.getIntOr("fly", 0);
		filter = in.read("flt", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		for (int i = 0; i < modules.size(); i++) {
			modules.set(i, ItemStack.EMPTY);
		}
		int i = 0;
		for (ValueInput child : in.childrenListOrEmpty("modules")) {
			if (i < modules.size()) {
				modules.set(i++, child.read("s", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
			}
		}
		craftPending = in.getIntOr("cp", 0);
		if (craftPending > 0) {
			craftOut = in.read("co", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
			craftDest = in.getLongOr("cd", Transit.NO_DEST);
			craftDestFace = in.getIntOr("cf", LogisticsNetwork.FACE_EJECT);
			craftInflight = in.getIntOr("ci", 0);
			craftBuffer.clear();
			for (ValueInput child : in.childrenListOrEmpty("buf")) {
				ItemStack b = child.read("s", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
				if (!b.isEmpty()) {
					craftBuffer.add(b);
				}
			}
		}
	}
}
