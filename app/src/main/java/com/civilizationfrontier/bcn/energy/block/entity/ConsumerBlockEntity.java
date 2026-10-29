package com.civilizationfrontier.bcn.energy.block.entity;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 検証用消費機. 届いたパケットを全量吸収するシンク.
 * totalReceived (NBT `total_received`) が累計消費量として検証に使える.
 */
public class ConsumerBlockEntity extends MjStorageBlockEntity {

	public ConsumerBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.CONSUMER, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ConsumerBlockEntity be) {
		// シンクのため tick 処理なし (受信は receive で完結)
	}

	@Override
	public double receive(double packetEnergy, Direction face) {
		if (packetEnergy <= 0 || packetEnergy > maxInputPacket()) {
			return 0;
		}
		totalReceived += packetEnergy;
		setChanged();
		return packetEnergy;
	}

	@Override
	protected double capacity() {
		return Double.MAX_VALUE;
	}

	@Override
	protected double maxInputPacket() {
		return MjPressure.EV.packetSize();
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
}
