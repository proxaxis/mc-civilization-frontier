package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.block.BcnPipeBlock;
import com.civilizationfrontier.bcn.energy.MjCable;
import com.civilizationfrontier.bcn.energy.MjPressure;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * MJ ケーブル. BcnPipeBlock の multipart 接続を流用し、
 * 許容パケット圧 (ランク) とブロックあたり損失を持つ.
 * パケット経路自体は BlockEntity を持たず MjNetwork の BFS がブロックを辿る.
 */
public class MjCableBlock extends BcnPipeBlock implements MjCable {

	private final MjPressure rank;
	private final double lossPerBlock;

	public MjCableBlock(MjPressure rank, double lossPerBlock, Properties properties) {
		super(properties, false);
		this.rank = rank;
		this.lossPerBlock = lossPerBlock;
	}

	public MjPressure rank() {
		return rank;
	}

	@Override
	public double maxPacket() {
		return rank.packetSize();
	}

	@Override
	public double lossPerBlock() {
		return lossPerBlock;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return null; // ケーブルは状態を持たない (経路探索はブロック単位で行う)
	}
}
