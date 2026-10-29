package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.MjGeneratorBlock;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 検証用発電機. tier ブロック状態に応じた MJ-P ランクで一定量を発電する.
 * 稼働中は EFP をスコアボードへ加算する (機械からの EFP 連携の実装例).
 */
public class GeneratorBlockEntity extends MjStorageBlockEntity {

	/** 発電量 (MJ/t). 火力発電機相当 (10 EU/t -> 4 MJ/t). */
	public static final double RATE = 4.0;

	/** EFP 加算係数: エンジン100台・3時間連続稼働で災害閾値 (60) 到達の設計値. */
	public static final double EFP_PER_TICK = 2.8e-6;

	private double producedTotal;

	public GeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.GENERATOR, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		be.energy = Math.min(be.capacity(), be.energy + RATE);
		be.producedTotal += RATE;
		be.efpBuffer += EFP_PER_TICK;
		if (be.efpBuffer >= 1.0) {
			long whole = (long) be.efpBuffer;
			be.efpBuffer -= whole;
			BcnEfp.add(level.getServer(), whole);
		}
		// 発電機は定格ランクのフルパケットを溜めてから送出する (パケットサイズ=圧ランク)
		if (be.energy >= be.maxOutputPacket()) {
			be.emit(level);
		}
		if (!state.getValue(MjMachineBlock.ACTIVE)) {
			level.setBlock(pos, state.setValue(MjMachineBlock.ACTIVE, true), Block.UPDATE_ALL);
		}
		be.setChanged();
	}

	public MjPressure tier() {
		return MjPressure.values()[getBlockState().getValue(MjGeneratorBlock.TIER)];
	}

	public double producedTotal() {
		return producedTotal;
	}

	@Override
	protected double capacity() {
		return tier().packetSize() * 4.0;
	}

	@Override
	protected double maxInputPacket() {
		return 0;
	}

	@Override
	protected double maxOutputPacket() {
		return tier().packetSize();
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
