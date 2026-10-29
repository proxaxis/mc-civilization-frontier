package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * クァーリー. MJ 消費で直下の範囲を層状に自動採掘する.
 * - 採掘は設置者 FakePlayer として発行し、Flan 保護地では掘らない
 * - 常駐チャンクロード無し: 採掘対象が未ロードなら待機する (進まない)
 * - ドロップは隣接コンテナ/パイプへ搬出
 */
public class QuarryBlockEntity extends MjStorageBlockEntity {

	public static final int AREA = 5;          // 5x5 (クァーリー中心)
	public static final int COST_MJ = 8;       // 1ブロックあたり消費 MJ
	public static final int INTERVAL = 4;      // 採掘間隔 tick
	private static final double EFP_PER_TICK = 2.8e-6;
	private static final GameProfile PROFILE = new GameProfile(UUID.randomUUID(), "[BCN-Quarry]");

	private UUID owner;
	private int cx, cy, cz;                    // 採掘カーソル
	private int cooldown;

	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.QUARRY, pos, state);
		cx = pos.getX() - AREA / 2;
		cy = pos.getY() - 1;
		cz = pos.getZ() - AREA / 2;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		if (--be.cooldown > 0 || be.energy < COST_MJ) {
			return;
		}
		if (!(level instanceof ServerLevel server)) {
			return;
		}
		ServerPlayer actor = FakePlayer.get(server, PROFILE);

		for (int guard = 0; guard < 25; guard++) {
			if (be.cy < level.getMinY()) {
				return; // 岩盤まで到達 = 完了
			}
			BlockPos target = new BlockPos(be.cx, be.cy, be.cz);
			be.advance(pos);
			if (!level.isLoaded(target)) {
				return; // 未ロード範囲では待機 (次 tick 再試行)
			}
			BlockState targetState = level.getBlockState(target);
			if (targetState.isAir() || !targetState.getFluidState().isEmpty()
					|| targetState.getDestroySpeed(level, target) < 0) {
				continue; // 空気・流体・岩盤等は読み飛ばす
			}
			if (!be.canMine(server, actor, target)) {
				continue; // Flan 保護地は掘らない
			}
			List<ItemStack> drops = Block.getDrops(targetState, server, target,
					level.getBlockEntity(target), actor, new ItemStack(Items.DIAMOND_PICKAXE));
			level.destroyBlock(target, false);
			be.eject(level, pos, drops);
			be.energy -= COST_MJ;
			be.produce();
			be.cooldown = INTERVAL;
			return;
		}
	}

	private void produce() {
		totalEmitted++; // 採掘ブロック数のカウンタとして流用
		efpBuffer += EFP_PER_TICK * INTERVAL;
		if (efpBuffer >= 1.0) {
			long whole = (long) efpBuffer;
			efpBuffer -= whole;
			BcnEfp.add(getLevel().getServer(), whole);
		}
		setChanged();
	}

	/** カーソルを層状 (y -> z -> x) に進める. */
	private void advance(BlockPos base) {
		cx++;
		if (cx > base.getX() + AREA / 2) {
			cx = base.getX() - AREA / 2;
			cz++;
			if (cz > base.getZ() + AREA / 2) {
				cz = base.getZ() - AREA / 2;
				cy--;
			}
		}
	}

	/** 採掘物を隣接コンテナ (チェスト/アイテムパイプ) へ搬出. 収まりきらない分は設置面上にドロップ. */
	private void eject(Level level, BlockPos pos, List<ItemStack> drops) {
		List<Container> targets = new ArrayList<>();
		for (Direction d : Direction.values()) {
			BlockPos np = pos.relative(d);
			if (level.getBlockEntity(np) instanceof Container c) {
				targets.add(c);
			}
		}
		for (ItemStack drop : drops) {
			if (drop.isEmpty()) {
				continue;
			}
			for (Container c : targets) {
				insertInto(c, drop);
				if (drop.isEmpty()) {
					break;
				}
			}
			if (!drop.isEmpty()) {
				Block.popResource(level, pos.above(), drop);
			}
		}
	}

	private static void insertInto(Container c, ItemStack stack) {
		int size = c.getContainerSize();
		for (int i = 0; i < size && !stack.isEmpty(); i++) {
			ItemStack slot = c.getItem(i);
			if (slot.isEmpty()) {
				c.setItem(i, stack.copy());
				stack.setCount(0);
			} else if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
				int move = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
				slot.grow(move);
				stack.shrink(move);
				c.setItem(i, slot);
			}
		}
	}

	/** Flan 保護判定: クレームがあれば設置者以外の土地では掘らない. 未導入/未クレームは許可. */
	private boolean canMine(ServerLevel level, ServerPlayer actor, BlockPos target) {
		try {
			Class<?> handler = Class.forName("io.github.flemmli97.flan.api.ClaimHandler");
			Object claim = null;
			for (String name : new String[] { "getClaim", "permToClaim", "getClaimAt" }) {
				try {
					Method m = handler.getMethod(name, net.minecraft.world.level.Level.class, BlockPos.class);
					Object r = m.invoke(null, level, target);
					claim = r instanceof Optional<?> o ? o.orElse(null) : r;
					break;
				} catch (NoSuchMethodException ignored) {
				}
			}
			if (claim == null) {
				return true;
			}
			Object claimOwner = claim.getClass().getMethod("getOwner").invoke(claim);
			if (claimOwner instanceof UUID uuid && (uuid.equals(owner) || (owner == null && uuid.equals(PROFILE.id())))) {
				return true;
			}
			try {
				Method can = claim.getClass().getMethod("canInteract", ServerPlayer.class,
						Identifier.class, BlockPos.class, boolean.class);
				return (boolean) can.invoke(claim, actor,
						Identifier.fromNamespaceAndPath("flan", "break"), target, false);
			} catch (NoSuchMethodException e) {
				return false; // 判定不可のクレームは保守的に保護
			}
		} catch (Throwable t) {
			return true; // Flan 未導入時は保護なし
		}
	}

	@Override
	protected double capacity() {
		return 128;
	}

	@Override
	protected double maxInputPacket() {
		return MjPressure.LV.packetSize();
	}

	@Override
	protected double maxOutputPacket() {
		return 0;
	}

	@Override
	protected Direction outputFace(BlockState state) {
		return Direction.NORTH;
	}

	@Override
	protected int packetsPerTick() {
		return 0;
	}

	@Override
	protected boolean acceptsInput(Direction face) {
		return true;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("cx", cx);
		output.putInt("cy", cy);
		output.putInt("cz", cz);
		if (owner != null) {
			output.putString("owner", owner.toString());
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		cx = input.getIntOr("cx", cx);
		cy = input.getIntOr("cy", cy);
		cz = input.getIntOr("cz", cz);
		String o = input.getStringOr("owner", "");
		owner = o.isEmpty() ? null : UUID.fromString(o);
	}
}
