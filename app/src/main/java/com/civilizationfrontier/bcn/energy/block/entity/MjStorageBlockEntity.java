package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.energy.MjNetwork;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * MJ 蓄積型 BlockEntity の基底.
 * 内部バッファに MJ を保持し、出力面へパケットを送出する共通動作を提供する.
 * 発電機 (自己生成) / 変圧器 (圧変換) / 蓄電器 (蓄電) はこれを継承し、
 * 消費機は本クラスの sink 実装として定義する.
 */
public abstract class MjStorageBlockEntity extends BlockEntity implements MjReceiver {

	protected double energy;
	protected double totalReceived;
	protected double totalEmitted;
	protected double efpBuffer;

	protected MjStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/** 内部バッファ容量 (MJ). */
	protected abstract double capacity();

	/** 受け入れ可能なパケット圧の上限 (MJ-P 換算値). */
	protected abstract double maxInputPacket();

	/** 1パケットあたりの出力上限 (MJ). */
	protected abstract double maxOutputPacket();

	/** 出力面. エネルギーはこの面からのみ流出する. */
	protected abstract Direction outputFace(BlockState state);

	/** 1 tick に送出するパケットの最大数. */
	protected abstract int packetsPerTick();

	/** MJ 入力を受け付けるか (発電機のように受信しない場合 false). */
	protected boolean acceptsInput(Direction face) {
		return face != outputFace(getBlockState());
	}

	@Override
	public double receive(double packetEnergy, Direction face) {
		if (!acceptsInput(face) || packetEnergy > maxInputPacket()) {
			return 0;
		}
		double accepted = Math.min(capacity() - energy, packetEnergy);
		if (accepted <= 0) {
			return 0;
		}
		energy += accepted;
		totalReceived += accepted;
		setChanged();
		return accepted;
	}

	/** 稼働表示 (ACTIVE) をバッファ残量と同期する. */
	protected void updateActive(Level level, BlockState state) {
		boolean active = energy > 1.0e-9;
		if (state.hasProperty(com.civilizationfrontier.bcn.energy.block.MjMachineBlock.ACTIVE)
				&& state.getValue(com.civilizationfrontier.bcn.energy.block.MjMachineBlock.ACTIVE) != active) {
			level.setBlock(worldPosition,
					state.setValue(com.civilizationfrontier.bcn.energy.block.MjMachineBlock.ACTIVE, active),
					net.minecraft.world.level.block.Block.UPDATE_ALL);
		}
	}

	/** 出力面へパケット送出. 1 tick に packetsPerTick 個まで. */
	protected void emit(Level level) {
		BlockState state = getBlockState();
		Direction out = outputFace(state);
		boolean dirty = false;
		for (int i = 0; i < packetsPerTick() && energy > 1.0e-9; i++) {
			double packet = Math.min(energy, maxOutputPacket());
			double sent = MjNetwork.sendPacket(level, worldPosition, out, packet, this);
			if (sent <= 0) {
				break;
			}
			energy -= sent;
			totalEmitted += sent;
			dirty = true;
		}
		if (dirty) {
			setChanged();
		}
	}

	public double energy() {
		return energy;
	}

	public double totalReceived() {
		return totalReceived;
	}

	public double totalEmitted() {
		return totalEmitted;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putDouble("mj", energy);
		output.putDouble("total_received", totalReceived);
		output.putDouble("total_emitted", totalEmitted);
		output.putDouble("efp_buffer", efpBuffer);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		energy = input.getDoubleOr("mj", 0);
		totalReceived = input.getDoubleOr("total_received", 0);
		totalEmitted = input.getDoubleOr("total_emitted", 0);
		efpBuffer = input.getDoubleOr("efp_buffer", 0);
	}
}
