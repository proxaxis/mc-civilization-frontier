package com.civilizationfrontier.bcn.block.entity;

import java.util.Map;

import com.civilizationfrontier.bcn.block.BcnEngineBlock;
import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.EngineKind;
import com.civilizationfrontier.bcn.energy.block.entity.MjStorageBlockEntity;
import com.civilizationfrontier.bcn.fluid.BcnFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 燃焼エンジンの BlockEntity. STONE は固体燃料 (ホッパー搬入可)、
 * COMBUSTION は流体燃料 (パイプ/バケツ) を消費して発電する.
 * レッドストーン信号がある間のみ稼働し、稼働中は EFP を加算する.
 */
public class EngineBlockEntity extends MjStorageBlockEntity implements WorldlyContainer {

	/** 固体燃料の燃焼時間 (tick). かまど換算値. */
	private static final Map<Item, Integer> FUEL_TICKS = Map.ofEntries(
			Map.entry(Items.COAL, 1600), Map.entry(Items.CHARCOAL, 1600),
			Map.entry(Items.COAL_BLOCK, 16000), Map.entry(Items.BLAZE_ROD, 2400),
			Map.entry(Items.DRIED_KELP_BLOCK, 4000), Map.entry(Items.STICK, 100),
			Map.entry(Items.OAK_PLANKS, 300), Map.entry(Items.OAK_LOG, 300),
			Map.entry(Items.OAK_SAPLING, 100));

	/** 流体燃料の発熱量 (MJ/バケツ). 黒オイルは低品質、金オイルは高品質. */
	public static final double OIL_ENERGY_PER_BUCKET = 3000;
	public static final double FUEL_ENERGY_PER_BUCKET = 12000;

	public static final long TANK_CAPACITY = 81000L * 4; // 4バケツ

	private static final double EFP_PER_TICK = 2.8e-6;
	private static final int[] FUEL_SLOT = {0};

	private final NonNullList<ItemStack> fuelSlot = NonNullList.withSize(1, ItemStack.EMPTY);
	private int burnTime;
	private Fluid tankFluid;
	private long tankAmount;
	private double tankDrain; // ドロップレット端数の蓄積
	private double producedTotal;

	public EngineBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.ENGINE, pos, state);
	}

	private EngineKind kind() {
		return ((BcnEngineBlock) getBlockState().getBlock()).kind();
	}

	public static int fuelTicks(ItemStack stack) {
		return FUEL_TICKS.getOrDefault(stack.getItem(), 0);
	}

	/** 手渡し燃料をスロットへ投入 (残量は渡し手に残る). */
	public boolean insertFuel(ItemStack stack) {
		ItemStack slot = fuelSlot.get(0);
		if (slot.isEmpty()) {
			fuelSlot.set(0, stack.copyWithCount(1));
		} else if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
			slot.grow(1);
		} else {
			return false;
		}
		stack.shrink(1);
		setChanged();
		return true;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, EngineBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		boolean running = false;
		if (level.hasNeighborSignal(pos)) {
			running = be.kind().fluidFueled() ? be.burnFluid() : be.burnSolid();
		}
		if (running) {
			be.efpBuffer += EFP_PER_TICK;
			if (be.efpBuffer >= 1.0) {
				long whole = (long) be.efpBuffer;
				be.efpBuffer -= whole;
				BcnEfp.add(level.getServer(), whole);
			}
		}
		if (be.energy >= be.maxOutputPacket()) {
			be.emit(level);
		}
		if (state.getValue(com.civilizationfrontier.bcn.energy.block.MjMachineBlock.ACTIVE) != running) {
			level.setBlock(pos, state.setValue(
					com.civilizationfrontier.bcn.energy.block.MjMachineBlock.ACTIVE, running),
					net.minecraft.world.level.block.Block.UPDATE_ALL);
		}
		if (running) {
			be.setChanged();
		}
	}

	/** 固体燃料を燃やす. 燃焼中なら燃焼時間を消費して発電. */
	private boolean burnSolid() {
		if (burnTime <= 0) {
			ItemStack slot = fuelSlot.get(0);
			if (slot.isEmpty()) {
				return false;
			}
			burnTime = fuelTicks(slot);
			slot.shrink(1);
			if (burnTime <= 0) {
				return false;
			}
		}
		burnTime--;
		return produce();
	}

	/** 流体燃料を消費して発電. */
	private boolean burnFluid() {
		if (tankAmount <= 0 || tankFluid == null) {
			return false;
		}
		double perBucket = tankFluid == BcnFluids.FUEL ? FUEL_ENERGY_PER_BUCKET : OIL_ENERGY_PER_BUCKET;
		tankDrain += kind().rate() * 81000.0 / perBucket;
		long drain = (long) tankDrain;
		if (drain <= 0) {
			return produce();
		}
		drain = Math.min(drain, tankAmount);
		tankDrain -= drain;
		tankAmount -= drain;
		return produce();
	}

	private boolean produce() {
		if (energy >= capacity()) {
			return false;
		}
		energy = Math.min(capacity(), energy + kind().rate());
		producedTotal += kind().rate();
		return true;
	}

	/** 流体燃料の補給用ストレージ (パイプ/バケツからの投入のみ). */
	public Storage<FluidVariant> fluidStorage(Direction face) {
		return new Storage<>() {
			@Override
			public long insert(FluidVariant resource, long maxAmount, TransactionContext tx) {
				if (resource == null || resource.isBlank() || maxAmount <= 0) {
					return 0;
				}
				Fluid fluid = resource.getFluid();
				if (fluid != BcnFluids.OIL && fluid != BcnFluids.FLOWING_OIL
						&& fluid != BcnFluids.FUEL && fluid != BcnFluids.FLOWING_FUEL) {
					return 0;
				}
				if (tankFluid != null && tankAmount > 0 && tankFluid != fluid) {
					return 0;
				}
				long room = TANK_CAPACITY - tankAmount;
				long accepted = Math.min(room, maxAmount);
				if (accepted > 0) {
					tx.addOuterCloseCallback(result -> {
						if (result.wasCommitted()) {
							tankFluid = fluid;
							tankAmount += accepted;
							setChanged();
						}
					});
				}
				return accepted;
			}

			@Override
			public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
				return 0; // エンジンは燃料を消費するのみ (回収不可)
			}

			@Override
			public java.util.Iterator<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<FluidVariant>> iterator() {
				return java.util.Collections.emptyIterator();
			}
		};
	}

	@Override
	protected double capacity() {
		return kind().outputRank().packetSize() * 4.0;
	}

	@Override
	protected double maxInputPacket() {
		return 0; // エンジンは MJ を受け取らない
	}

	@Override
	protected double maxOutputPacket() {
		return kind().outputRank().packetSize();
	}

	@Override
	protected Direction outputFace(BlockState state) {
		return state.getValue(com.civilizationfrontier.bcn.energy.block.MjMachineBlock.FACING);
	}

	@Override
	protected int packetsPerTick() {
		return 1;
	}

	@Override
	protected boolean acceptsInput(Direction face) {
		return false;
	}

	public double producedTotal() {
		return producedTotal;
	}

	// ---- WorldlyContainer (ホッパー等からの固体燃料搬入) ----

	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return fuelSlot.get(0).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return fuelSlot.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return ContainerHelper.removeItem(fuelSlot, slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(fuelSlot, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		fuelSlot.set(slot, stack);
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		fuelSlot.clear();
		setChanged();
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return FUEL_SLOT;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return fuelTicks(stack) > 0;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return false;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("burn_time", burnTime);
		output.putDouble("produced_total", producedTotal);
		ContainerHelper.saveAllItems(output, fuelSlot);
		if (tankFluid != null && tankAmount > 0) {
			output.putString("tank_fluid", BuiltInRegistries.FLUID.getKey(tankFluid).toString());
		}
		output.putLong("tank_amount", tankAmount);
		output.putDouble("tank_drain", tankDrain);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		burnTime = input.getIntOr("burn_time", 0);
		producedTotal = input.getDoubleOr("produced_total", 0);
		ContainerHelper.loadAllItems(input, fuelSlot);
		Identifier fluidId = Identifier.tryParse(input.getStringOr("tank_fluid", ""));
		Fluid loaded = fluidId == null ? null : BuiltInRegistries.FLUID.getValue(fluidId);
		tankFluid = loaded == null || loaded == Fluids.EMPTY ? null : loaded;
		tankAmount = input.getLongOr("tank_amount", 0);
		tankDrain = input.getDoubleOr("tank_drain", 0);
	}
}
