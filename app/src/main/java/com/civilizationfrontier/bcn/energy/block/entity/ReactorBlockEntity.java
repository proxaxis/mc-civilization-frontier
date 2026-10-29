package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 原子炉. ウランインゴットを消費して 2 MJ/t を発電する (IC2 原子炉 5 EU/t -> 2 MJ/t).
 * 稼働中に熱が蓄積し、上限超過または高温での燃料切れでメルトダウン
 * (爆発 + ブロック破壊 + EFP 大量加算) を起こす.
 * 熱は停止中に冷めるため、パルス運転で安全に運用できる.
 */
public class ReactorBlockEntity extends MjStorageBlockEntity {

	public static final double RATE = 2.0;
	public static final int BURN_TICKS = 6000;       // インゴット1個 = 5分
	public static final double HEAT_MAX = 1200;      // 連続 60 秒稼働で臨界
	public static final double HEAT_PER_TICK = 1.0;
	public static final double COOL_PER_TICK = 1.0;
	public static final double HEAT_FUEL_OUT_SPIKE = 400;
	public static final long MELTDOWN_EFP = 500;
	public static final float MELTDOWN_RADIUS = 8.0f;

	private final NonNullList<ItemStack> fuelSlot = NonNullList.withSize(1, ItemStack.EMPTY);
	private int burnTime;
	private double heat;
	private double producedTotal;

	public ReactorBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.REACTOR, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ReactorBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		boolean running = false;
		if (be.burnTime <= 0) {
			running = be.startBurn(level, pos);
		} else {
			running = true;
		}
		if (running) {
			be.burnTime--;
			be.heat += HEAT_PER_TICK;
			be.energy = Math.min(be.capacity(), be.energy + RATE);
			be.producedTotal += RATE;
		} else {
			be.heat = Math.max(0, be.heat - COOL_PER_TICK);
		}
		if (be.heat >= HEAT_MAX) {
			meltdown(level, pos, be);
			return;
		}
		if (be.energy >= be.maxOutputPacket()) {
			be.emit(level);
		}
		if (state.getValue(MjMachineBlock.ACTIVE) != running) {
			level.setBlock(pos, state.setValue(MjMachineBlock.ACTIVE, running), Block.UPDATE_ALL);
		}
		be.setChanged();
	}

	/** 新しい燃料燃焼を開始. 燃料切れ時は熱スパイクを与え、高温なら臨界へ. */
	private boolean startBurn(Level level, BlockPos pos) {
		ItemStack slot = fuelSlot.get(0);
		if (slot.isEmpty()) {
			heat += HEAT_FUEL_OUT_SPIKE;
			return false;
		}
		slot.shrink(1);
		burnTime = BURN_TICKS;
		return true;
	}

	private static void meltdown(Level level, BlockPos pos, ReactorBlockEntity be) {
		BcnEfp.add(level.getServer(), MELTDOWN_EFP);
		Containers.dropContents(level, pos, be.fuelSlot);
		level.removeBlock(pos, false);
		level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0f, 0.7f);
		level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				MELTDOWN_RADIUS, Level.ExplosionInteraction.BLOCK);
	}

	/** 燃料投入 (ホッパー/手渡し). ウランインゴットのみ. */
	public boolean insertFuel(ItemStack stack) {
		if (!stack.is(BcnItems.URANIUM_INGOT)) {
			return false;
		}
		ItemStack slot = fuelSlot.get(0);
		if (slot.isEmpty()) {
			fuelSlot.set(0, stack.copyWithCount(1));
		} else if (ItemStack.isSameItemSameComponents(slot, stack)
				&& slot.getCount() < slot.getMaxStackSize()) {
			slot.grow(1);
		} else {
			return false;
		}
		stack.shrink(1);
		setChanged();
		return true;
	}

	public double heat() {
		return heat;
	}

	public int burnTime() {
		return burnTime;
	}

	public ItemStack fuelStack() {
		return fuelSlot.get(0);
	}

	@Override
	protected double capacity() {
		return MjPressure.LV.packetSize() * 4.0;
	}

	@Override
	protected double maxInputPacket() {
		return 0;
	}

	@Override
	protected double maxOutputPacket() {
		return MjPressure.LV.packetSize();
	}

	@Override
	protected Direction outputFace(BlockState state) {
		return state.getValue(MjMachineBlock.FACING);
	}

	@Override
	protected int packetsPerTick() {
		return 1;
	}

	@Override
	protected boolean acceptsInput(Direction face) {
		return false;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("burn_time", burnTime);
		output.putDouble("heat", heat);
		output.putDouble("produced_total", producedTotal);
		net.minecraft.world.ContainerHelper.saveAllItems(output, fuelSlot, true);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		burnTime = input.getIntOr("burn_time", 0);
		heat = input.getDoubleOr("heat", 0);
		producedTotal = input.getDoubleOr("produced_total", 0);
		net.minecraft.world.ContainerHelper.loadAllItems(input, fuelSlot);
	}
}
