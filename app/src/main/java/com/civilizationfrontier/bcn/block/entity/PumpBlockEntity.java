package com.civilizationfrontier.bcn.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.entity.MjStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** ポンプ. MJ 消費で直下の流体ソースを汲み上げ、隣接する液体ストレージ(防水パイプ等)へ送出. */
public class PumpBlockEntity extends MjStorageBlockEntity {

	public static final double COST_MJ = 10;       // 汲み上げ 1 バケツあたり
	public static final int INTERVAL = 8;          // 動作間隔 tick
	public static final int SCAN_DEPTH = 64;
	public static final long BUFFER = FluidConstants.BUCKET * 4;
	private static final double EFP_PER_TICK = 2.8e-6;

	private FluidVariant variant = FluidVariant.blank();
	private long amount;
	private int cooldown;

	public PumpBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.PUMP, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, PumpBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		boolean active = false;
		if (--be.cooldown <= 0) {
			be.cooldown = INTERVAL;
			active = be.work(level, pos);
			active |= be.push(level, pos);
		}
		if (active) {
			be.efpBuffer += EFP_PER_TICK * INTERVAL;
			if (be.efpBuffer >= 1.0) {
				long whole = (long) be.efpBuffer;
				be.efpBuffer -= whole;
				BcnEfp.add(level.getServer(), whole);
			}
		}
		be.updateActive(level, state);
	}

	/** 直下を走査して最初に見つかった流体ソースを 1 つ汲み上げる. */
	private boolean work(Level level, BlockPos pos) {
		if (amount >= BUFFER || energy < COST_MJ) {
			return false;
		}
		for (int i = 1; i <= SCAN_DEPTH; i++) {
			BlockPos target = pos.below(i);
			if (!level.isLoaded(target)) {
				return false;
			}
			FluidState fluid = level.getFluidState(target);
			if (fluid.isEmpty()) {
				continue;
			}
			if (!fluid.isSource()) {
				return false; // 流れ途中の部分ブロックでは吸わない
			}
			FluidVariant v = FluidVariant.of(fluid.getType());
			if (!variant.isBlank() && !variant.equals(v)) {
				return false;
			}
			level.setBlock(target, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
			variant = v;
			amount += FluidConstants.BUCKET;
			energy -= COST_MJ;
			setChanged();
			return true;
		}
		return false;
	}

	/** 内部バッファを隣接する液体ストレージへ押し出す. */
	private boolean push(Level level, BlockPos pos) {
		if (amount <= 0) {
			return false;
		}
		boolean moved = false;
		for (Direction d : Direction.values()) {
			Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos.relative(d), d.getOpposite());
			if (storage == null) {
				continue;
			}
			try (Transaction tx = Transaction.openOuter()) {
				long inserted = storage.insert(variant, amount, tx);
				if (inserted > 0) {
					tx.commit();
					amount -= inserted;
					moved = true;
				}
			}
			if (amount <= 0) {
				break;
			}
		}
		if (moved) {
			if (amount <= 0) {
				variant = FluidVariant.blank();
			}
			setChanged();
		}
		return moved;
	}

	public Storage<FluidVariant> fluidStorage(Direction face) {
		PumpBlockEntity self = this;
		return new Storage<>() {
			@Override public long insert(FluidVariant resource, long maxAmount, TransactionContext tx) { return 0; }
			@Override public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
				if (resource == null || !resource.equals(variant) || amount <= 0 || maxAmount <= 0) {
					return 0;
				}
				long taken = Math.min(amount, maxAmount);
				tx.addOuterCloseCallback(r -> {
					if (r.wasCommitted()) {
						amount -= taken;
						if (amount <= 0) {
							variant = FluidVariant.blank();
						}
						setChanged();
					}
				});
				return taken;
			}
			@Override public Iterator<StorageView<FluidVariant>> iterator() {
				return java.util.List.<StorageView<FluidVariant>>of(new StorageView<FluidVariant>() {
					@Override public long extract(FluidVariant r, long max, TransactionContext tx) { return 0; }
					@Override public boolean isResourceBlank() { return amount <= 0; }
					@Override public FluidVariant getResource() { return variant; }
					@Override public long getAmount() { return amount; }
					@Override public long getCapacity() { return BUFFER; }
				}).iterator();
			}
		};
	}

	@Override protected double capacity() { return MjPressure.LV.packetSize() * 20.0; }
	@Override protected double maxInputPacket() { return MjPressure.LV.packetSize(); }
	@Override protected double maxOutputPacket() { return 0; }
	@Override protected Direction outputFace(BlockState state) { return Direction.NORTH; }
	@Override protected int packetsPerTick() { return 0; }
	@Override protected boolean acceptsInput(Direction face) { return true; }

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		out.putString("fl", variant.isBlank() ? "" : BuiltInRegistries.FLUID.getKey(variant.getFluid()).toString());
		out.putLong("amt", amount);
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		String id = in.getStringOr("fl", "");
		Identifier fid = id.isEmpty() ? null : Identifier.tryParse(id);
		Fluid fluid = fid == null ? null : BuiltInRegistries.FLUID.getValue(fid);
		variant = fluid == null ? FluidVariant.blank() : FluidVariant.of(fluid);
		amount = fluid == null ? 0 : in.getLongOr("amt", 0);
	}
}
