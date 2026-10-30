package com.civilizationfrontier.bcn.energy.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.civilizationfrontier.bcn.block.entity.AssemblyTableBlockEntity;
import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.MjMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * レーザー. MJ 受電して近傍の精密作業台へエネルギーを照射する.
 * 4 MJ/t を取り込み、5 ブロック以内の assembly_table へ均等分配する.
 * 照射先の座標は描画用にキャッシュする (BlockEntityRenderer のビーム).
 */
public class LaserBlockEntity extends MjStorageBlockEntity {

	public static final double MAX_RATE = 4.0;  // 最大消費 MJ/t
	public static final int RANGE = 5;          // 照射半径

	private final List<BlockPos> targets = new ArrayList<>();
	private int scanCooldown;
	private double suppliedTotal;

	public LaserBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.LASER, pos, state);
	}

	public List<BlockPos> targets() {
		return targets;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, LaserBlockEntity be) {
		if (level.isClientSide()) {
			return;
		}
		if (--be.scanCooldown <= 0) {
			be.scanCooldown = 20;
			be.scan(level, pos);
		}
		if (be.targets.isEmpty() || be.energy <= 0) {
			be.updateActive(level, state);
			return;
		}
		double per = Math.min(be.energy, MAX_RATE) / be.targets.size();
		for (BlockPos tp : be.targets) {
			if (level.getBlockEntity(tp) instanceof AssemblyTableBlockEntity table) {
				double room = AssemblyTableBlockEntity.CAPACITY - table.energy();
				double give = Math.min(per, room);
				if (give > 0) {
					table.addEnergy(give);
					be.energy -= give;
					be.suppliedTotal += give;
				}
			}
		}
		be.updateActive(level, state);
		be.setChanged();
	}

	private void scan(Level level, BlockPos pos) {
		targets.clear();
		for (BlockPos tp : BlockPos.betweenClosed(pos.offset(-RANGE, -RANGE, -RANGE),
				pos.offset(RANGE, RANGE, RANGE))) {
			if (level.getBlockEntity(tp) instanceof AssemblyTableBlockEntity) {
				targets.add(tp.immutable());
			}
		}
	}

	@Override
	protected double capacity() {
		return 500.0;
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
		output.putDouble("supplied_total", suppliedTotal);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		suppliedTotal = input.getDoubleOr("supplied_total", 0);
	}
}
