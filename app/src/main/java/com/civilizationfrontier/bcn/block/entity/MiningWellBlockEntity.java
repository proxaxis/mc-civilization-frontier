package com.civilizationfrontier.bcn.block.entity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import com.civilizationfrontier.bcn.energy.block.entity.MjStorageBlockEntity;
import com.civilizationfrontier.bcn.energy.block.entity.QuarryBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** ボーリングマシン. MJ 消費で直下を 1 ブロックずつ掘削 (クァーリーの単幅版). ドロップは背面へ排出. */
public class MiningWellBlockEntity extends MjStorageBlockEntity {

	public static final int COST_MJ = 8;
	public static final int INTERVAL = 4;
	private static final double EFP_PER_TICK = 2.8e-6;
	private static final GameProfile PROFILE = new GameProfile(UUID.randomUUID(), "[BCN-MiningWell]");

	private UUID owner;
	private int cy;
	private int cooldown;

	public MiningWellBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.MINING_WELL, pos, state);
		cy = pos.getY() - 1;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, MiningWellBlockEntity be) {
		if (level.isClientSide() || --be.cooldown > 0 || be.energy < COST_MJ || !(level instanceof ServerLevel server)) {
			return;
		}
		if (be.cy < level.getMinY()) {
			return;
		}
		BlockPos target = new BlockPos(pos.getX(), be.cy, pos.getZ());
		be.cy--;
		if (!level.isLoaded(target)) {
			return;
		}
		BlockState targetState = level.getBlockState(target);
		if (targetState.isAir() || !targetState.getFluidState().isEmpty()
				|| targetState.getDestroySpeed(level, target) < 0 || !be.canMine(server, target)) {
			be.cooldown = 1;
			return;
		}
		ServerPlayer actor = FakePlayer.get(server, PROFILE);
		List<ItemStack> drops = Block.getDrops(targetState, server, target,
				level.getBlockEntity(target), actor, new ItemStack(Items.DIAMOND_PICKAXE));
		level.destroyBlock(target, false);
		be.eject(level, pos, state, drops);
		be.energy -= COST_MJ;
		be.totalEmitted++;
		be.efpBuffer += EFP_PER_TICK * INTERVAL;
		if (be.efpBuffer >= 1.0) {
			long whole = (long) be.efpBuffer;
			be.efpBuffer -= whole;
			BcnEfp.add(level.getServer(), whole);
		}
		be.cooldown = INTERVAL;
		be.setChanged();
		be.updateActive(level, state);
	}

	/** ドロップを背面 (FACING の反対面) の容器/パイプへ排出. 収まらない分は設置面上へドロップ. */
	private void eject(Level level, BlockPos pos, BlockState state, List<ItemStack> drops) {
		Direction back = state.getValue(MjMachineBlock.FACING).getOpposite();
		Container target = HopperBlockEntity.getContainerAt(level, pos.relative(back));
		for (ItemStack drop : drops) {
			if (drop.isEmpty()) {
				continue;
			}
			if (target != null) {
				drop = HopperBlockEntity.addItem(this instanceof Container c ? c : null, target, drop, back.getOpposite());
			}
			if (!drop.isEmpty()) {
				Block.popResource(level, pos.above(), drop);
			}
		}
	}

	private boolean canMine(ServerLevel level, BlockPos target) {
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
			return claimOwner instanceof UUID uuid && (uuid.equals(owner)
					|| (owner == null && uuid.equals(PROFILE.id())));
		} catch (Throwable t) {
			return true;
		}
	}

	@Override protected double capacity() { return 128; }
	@Override protected double maxInputPacket() { return MjPressure.LV.packetSize(); }
	@Override protected double maxOutputPacket() { return 0; }
	@Override protected Direction outputFace(BlockState state) { return Direction.NORTH; }
	@Override protected int packetsPerTick() { return 0; }
	@Override protected boolean acceptsInput(Direction face) { return true; }

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		out.putInt("cy", cy);
		if (owner != null) out.putString("owner", owner.toString());
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		cy = in.getIntOr("cy", cy);
		String o = in.getStringOr("owner", "");
		owner = o.isEmpty() ? null : UUID.fromString(o);
	}
}
