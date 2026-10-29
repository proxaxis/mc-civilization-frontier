package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 環境発電機の共通基底 (水力/風力/ソーラー).
 * 環境条件は 20 tick 毎に再評価し、その間はキャッシュしたレートで発電する.
 * クリーン電力の EFP は稼働中ごく僅かに減算する.
 */
public abstract class EnvGeneratorBlockEntity extends MjStorageBlockEntity {

	/** 稼働中の EFP 減算レート (負値). 丸まって -1 になった時点で適用する. */
	private static final double EFP_PER_TICK = -1.0e-6;

	double producedTotal;
	double cachedRate;
	int envCooldown;

	protected EnvGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/** 環境条件から算出する現在の発電レート (MJ/t). 20 tick 毎に評価される. */
	protected abstract double computeRate(Level level, BlockPos pos, BlockState state);

	/** 出力パケットランク. */
	protected abstract MjPressure outputRank();

	public static <T extends EnvGeneratorBlockEntity> void tick(Level level, BlockPos pos,
			BlockState state, T be) {
		if (level.isClientSide()) {
			return;
		}
		if (--be.envCooldown <= 0) {
			be.envCooldown = 20;
			be.cachedRate = be.computeRate(level, pos, state);
		}
		boolean running = be.cachedRate > 0 && be.energy < be.capacity();
		if (running) {
			be.energy = Math.min(be.capacity(), be.energy + be.cachedRate);
			be.producedTotal += be.cachedRate;
			be.efpBuffer += EFP_PER_TICK;
			if (be.efpBuffer <= -1.0) {
				long whole = (long) be.efpBuffer;
				be.efpBuffer -= whole;
				BcnEfp.add(level.getServer(), whole);
			}
		}
		if (be.energy >= be.maxOutputPacket()) {
			be.emit(level);
		}
		if (state.getValue(MjMachineBlock.ACTIVE) != running) {
			level.setBlock(pos, state.setValue(MjMachineBlock.ACTIVE, running), Block.UPDATE_ALL);
		}
		if (running) {
			be.setChanged();
		}
	}

	public double producedTotal() {
		return producedTotal;
	}

	public double currentRate() {
		return cachedRate;
	}

	@Override
	protected double capacity() {
		return outputRank().packetSize() * 4.0;
	}

	@Override
	protected double maxInputPacket() {
		return 0;
	}

	@Override
	protected double maxOutputPacket() {
		return outputRank().packetSize();
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
		output.putDouble("produced_total", producedTotal);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		producedTotal = input.getDoubleOr("produced_total", 0);
	}
}
