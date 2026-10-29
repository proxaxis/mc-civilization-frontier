package com.civilizationfrontier.bcn.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.civilizationfrontier.bcn.block.BcnPipeBlock;
import com.civilizationfrontier.bcn.block.BcnPipeBlock.Conn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ItemPipeBlockEntity extends BlockEntity implements WorldlyContainer {

	public static final float SPEED = 0.1F; // 1ブロックあたり10tick
	private static final int MAX_TRANSIT = 8;
	private static final int[] SLOTS = { 0 };

	public static final class Transit {
		public static final long NO_DEST = Long.MIN_VALUE;

		public ItemStack stack = ItemStack.EMPTY;
		public Direction from = Direction.UP;
		public Direction to = Direction.DOWN;
		public float progress;
		public boolean turned;
		/** 配送先パイプ位置 (BlockPos.asLong). NO_DEST なら無宛先 (ランダム網走行). */
		public long dest = NO_DEST;
		/** 配送先での搬出面. 6 以上はパイプ内部バッファへの直接配送を意味する. */
		public int destFace = -1;
		/** 経路の残り出口方向 (各パイプ通過時に先頭から消費). */
		public Direction[] route = new Direction[0];
		public int routeIdx;

		public boolean hasRoute() {
			return routeIdx < route.length || dest != NO_DEST;
		}
	}

	private final List<Transit> transit = new ArrayList<>();
	private Direction pendingFrom;

	public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
		this(MachineBlockEntities.ITEM_PIPE, pos, state);
	}

	protected ItemPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public List<Transit> transit() {
		return transit;
	}

	public boolean addTransit(ItemStack stack, Direction from) {
		return addTransit(stack, from, Transit.NO_DEST, -1, List.of());
	}

	/** 宛先付き Transit を投入する. destFace>=6 は宛先パイプ内部への直接配送. */
	public boolean addTransit(ItemStack stack, Direction from, long dest, int destFace, List<Direction> route) {
		if (stack.isEmpty() || transit.size() >= MAX_TRANSIT) {
			return false;
		}
		Transit t = new Transit();
		t.stack = stack.copy();
		t.from = from;
		t.to = from.getOpposite();
		t.dest = dest;
		t.destFace = destFace;
		t.route = route.toArray(new Direction[0]);
		transit.add(t);
		markDirtyAndSync();
		return true;
	}

	public void tick(Level level, BlockPos pos) {
		boolean sync = false;
		for (int i = transit.size() - 1; i >= 0; i--) {
			Transit t = transit.get(i);
			t.progress += SPEED;
			if (!t.turned && t.progress >= 0.5F) {
				t.to = chooseExit(level, pos, t);
				t.turned = true;
				sync = true;
			}
			if (t.progress >= 1.0F) {
				switch (handOff(level, pos, t)) {
				case REMOVE -> {
					transit.remove(i);
					sync = true;
				}
				case BOUNCE -> sync = true;
				case HOLD -> t.progress = 1.0F;
				}
			}
		}
		if (sync) {
			markDirtyAndSync();
		}
	}

	private enum HandOff {
		REMOVE, BOUNCE, HOLD
	}

	/**
	 * 出口決定. 宛先付き Transit は経路を辿り、宛先パイプでは destFace へ向かう.
	 * サブクラスは無宛先アイテムへ宛先を割り当てるためオーバーライドできる.
	 */
	protected Direction chooseExit(Level level, BlockPos pos, Transit t) {
		if (t.dest != Transit.NO_DEST && BlockPos.of(t.dest).equals(pos) && t.destFace >= 0) {
			return t.destFace < Direction.values().length ? Direction.values()[t.destFace] : t.from;
		}
		if (t.routeIdx < t.route.length) {
			Direction next = t.route[t.routeIdx++];
			if (getBlockState().getValue(BcnPipeBlock.property(next)) != Conn.NONE) {
				return next;
			}
			// 経路が実態と食い違った (配管変更) → 以降は無宛先扱い
			t.dest = Transit.NO_DEST;
			t.routeIdx = t.route.length;
		}
		return randomExit(t.from);
	}

	private Direction randomExit(Direction from) {
		BlockState state = getBlockState();
		List<Direction> exits = new ArrayList<>();
		for (Direction d : Direction.values()) {
			if (d != from && state.getValue(BcnPipeBlock.property(d)) != Conn.NONE) {
				exits.add(d);
			}
		}
		return exits.isEmpty() ? from : exits.get(level.getRandom().nextInt(exits.size()));
	}

	private HandOff handOff(Level level, BlockPos pos, Transit t) {
		if (t.dest != Transit.NO_DEST && BlockPos.of(t.dest).equals(pos)) {
			return deliver(level, pos, t);
		}
		BlockPos targetPos = pos.relative(t.to);
		BlockState targetState = level.getBlockState(targetPos);
		if (targetState.getBlock() instanceof BcnPipeBlock pipe && !pipe.fluid
				&& level.getBlockEntity(targetPos) instanceof ItemPipeBlockEntity other) {
			if (other.transit.size() < MAX_TRANSIT) {
				t.from = t.to.getOpposite();
				t.to = t.from.getOpposite();
				t.progress = 0;
				t.turned = false;
				other.transit.add(t);
				other.markDirtyAndSync();
				return HandOff.REMOVE;
			}
			return HandOff.HOLD;
		}
		Container container = HopperBlockEntity.getContainerAt(level, targetPos);
		if (container != null && container != this) {
			t.stack = HopperBlockEntity.addItem(this, container, t.stack, t.to.getOpposite());
			if (t.stack.isEmpty()) {
				return HandOff.REMOVE;
			}
			bounce(t);
			return HandOff.BOUNCE;
		}
		if (getBlockState().getValue(BcnPipeBlock.property(t.to)) == Conn.NONE) {
			eject(level, pos, t);
			return HandOff.REMOVE;
		}
		bounce(t);
		return HandOff.BOUNCE;
	}

	/**
	 * 宛先パイプへ到達した Transit の最終配送.
	 * destFace>=6 は内部バッファ配送、有効方向ならその先のコンテナへ、
	 * 残りや失敗は無宛先に戻して再ルーティングに委ねる.
	 */
	private HandOff deliver(Level level, BlockPos pos, Transit t) {
		onDeliveryArrived(t.stack);
		if (t.destFace >= Direction.values().length) {
			t.stack = internalDelivery(t.stack);
			if (!t.stack.isEmpty()) {
				eject(level, pos, t);
			}
			return HandOff.REMOVE;
		}
		if (t.destFace >= 0) {
			Direction face = Direction.values()[t.destFace];
			Container container = HopperBlockEntity.getContainerAt(level, pos.relative(face));
			if (container != null && container != this) {
				t.stack = HopperBlockEntity.addItem(this, container, t.stack, face.getOpposite());
			}
			if (!t.stack.isEmpty()) {
				// 溢れ分は宛先を失って網内を再度流れる (別シンクで再ルーティングされる)
				t.dest = Transit.NO_DEST;
				t.destFace = -1;
				t.route = new Direction[0];
				t.routeIdx = 0;
				bounce(t);
				return HandOff.BOUNCE;
			}
			return HandOff.REMOVE;
		}
		eject(level, pos, t);
		return HandOff.REMOVE;
	}

	/** 宛先到達時コールバック (要求元の inflight 減算など). 既定は何もしない. */
	protected void onDeliveryArrived(ItemStack stack) {
	}

	/** destFace>=6 の内部配送先. 受け入れられなかった分を返す. 既定は全拒否. */
	protected ItemStack internalDelivery(ItemStack stack) {
		return stack;
	}

	private static void bounce(Transit t) {
		Direction back = t.from;
		t.from = t.to;
		t.to = back;
		t.progress = 0;
		t.turned = false;
	}

	private static void eject(Level level, BlockPos pos, Transit t) {
		Vec3 at = Vec3.atCenterOf(pos).add(t.to.getStepX() * 0.6, t.to.getStepY() * 0.6, t.to.getStepZ() * 0.6);
		ItemEntity entity = new ItemEntity(level, at.x, at.y, at.z, t.stack.copy());
		entity.setDeltaMovement(t.to.getStepX() * 0.15, t.to.getStepY() * 0.15, t.to.getStepZ() * 0.15);
		entity.setDefaultPickUpDelay();
		level.addFreshEntity(entity);
	}

	public void markDirtyAndSync() {
		setChanged();
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		ValueOutput.ValueOutputList list = out.childrenList("transit");
		for (Transit t : transit) {
			ValueOutput child = list.addChild();
			child.putInt("f", t.from.ordinal());
			child.putInt("t", t.to.ordinal());
			child.putBoolean("r", t.turned);
			child.putFloat("p", t.progress);
			child.store("s", ItemStack.CODEC, t.stack);
			if (t.dest != Transit.NO_DEST) {
				child.putLong("d", t.dest);
				child.putInt("df", t.destFace);
			}
			if (t.routeIdx < t.route.length) {
				ValueOutput.ValueOutputList rl = child.childrenList("rt");
				for (int i = t.routeIdx; i < t.route.length; i++) {
					rl.addChild().putInt("d", t.route[i].ordinal());
				}
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		transit.clear();
		for (ValueInput child : in.childrenListOrEmpty("transit")) {
			Transit t = new Transit();
			t.from = Direction.values()[child.getIntOr("f", 0)];
			t.to = Direction.values()[child.getIntOr("t", 0)];
			t.turned = child.getBooleanOr("r", false);
			t.progress = child.getFloatOr("p", 0);
			t.stack = child.read("s", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
			long dest = child.getLongOr("d", Transit.NO_DEST);
			if (dest != Transit.NO_DEST) {
				t.dest = dest;
				t.destFace = child.getIntOr("df", -1);
			}
			List<Direction> route = new ArrayList<>();
			for (ValueInput rc : child.childrenListOrEmpty("rt")) {
				route.add(Direction.values()[rc.getIntOr("d", 0)]);
			}
			t.route = route.toArray(new Direction[0]);
			if (!t.stack.isEmpty()) {
				transit.add(t);
			}
		}
	}

	@Override
	public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider lookup) {
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lookup);
		saveAdditional(out);
		return out.buildResult();
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level != null && !level.isClientSide()) {
			for (Transit t : transit) {
				Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, t.stack);
			}
			transit.clear();
		}
	}

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (level != null && !level.isClientSide()) {
			Direction from = pendingFrom != null ? pendingFrom : firstConnection();
			pendingFrom = null;
			addTransit(stack, from);
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return transit.size() < MAX_TRANSIT;
	}

	@Override
	public boolean stillValid(Player player) {
		return false;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return SLOTS;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		if (side == null || transit.size() >= MAX_TRANSIT) {
			return false;
		}
		pendingFrom = side;
		return true;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return false;
	}

	@Override
	public void clearContent() {
		pendingFrom = null;
	}

	private Direction firstConnection() {
		BlockState state = getBlockState();
		for (Direction d : Direction.values()) {
			if (state.getValue(BcnPipeBlock.property(d)) != Conn.NONE) {
				return d;
			}
		}
		return Direction.UP;
	}
}
