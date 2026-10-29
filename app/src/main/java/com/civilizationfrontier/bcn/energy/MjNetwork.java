package com.civilizationfrontier.bcn.energy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * MJ パケットの配送経路解決.
 * 送信者が出力面へパケットを1個出すたびに呼ばれ、隣接受信機への直送か
 * ケーブル網への BFS 探索で最短ホップの受信先へ届ける.
 * 網状態を持たない (パケット都度探索) ので常時シミュレーションは不要.
 */
public final class MjNetwork {

	private MjNetwork() {
	}

	private record Candidate(BlockPos pos, MjReceiver receiver, Direction face, double loss) {
	}

	/**
	 * パケット1個を from の face 面から送出する.
	 *
	 * @param level   ワールド (サーバー側のみ有効)
	 * @param from    送信ブロック位置
	 * @param face    出力面
	 * @param energy  パケットのエネルギー量 = パケット圧 (MJ)
	 * @param exclude 送信者自身の BlockEntity (自己受信ループ防止). null 可
	 * @return 配送できた量 (MJ). 0 は全損失/拒否
	 */
	public static double sendPacket(Level level, BlockPos from, Direction face, double energy, BlockEntity exclude) {
		if (level.isClientSide() || energy <= 0) {
			return 0;
		}
		BlockPos start = from.relative(face);

		BlockEntity startBe = level.getBlockEntity(start);
		if (startBe instanceof MjReceiver direct && startBe != exclude) {
			return direct.receive(energy, face.getOpposite());
		}
		if (!(level.getBlockState(start).getBlock() instanceof MjCable startCable)) {
			return 0;
		}
		if (energy > startCable.maxPacket()) {
			burnCable(level, start);
			return 0;
		}

		Set<BlockPos> visited = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		Map<BlockPos, Double> lossAt = new HashMap<>();
		List<BlockPos> burn = new ArrayList<>();
		List<Candidate> candidates = new ArrayList<>();
		visited.add(start);
		visited.add(from);
		queue.add(start);
		lossAt.put(start, startCable.lossPerBlock());

		while (!queue.isEmpty()) {
			BlockPos cur = queue.poll();
			double curLoss = lossAt.get(cur);
			for (Direction d : Direction.values()) {
				BlockPos next = cur.relative(d);
				if (!visited.add(next)) {
					continue;
				}
				if (level.getBlockState(next).getBlock() instanceof MjCable cable) {
					if (energy > cable.maxPacket()) {
						burn.add(next);
						continue;
					}
					lossAt.put(next, curLoss + cable.lossPerBlock());
					queue.add(next);
				} else {
					BlockEntity be = level.getBlockEntity(next);
					if (be instanceof MjReceiver receiver && be != exclude) {
						candidates.add(new Candidate(next, receiver, d.getOpposite(), curLoss));
					}
				}
			}
		}

		for (BlockPos pos : burn) {
			burnCable(level, pos);
		}
		for (Candidate c : candidates) {
			double delivered = energy - c.loss();
			if (delivered <= 0) {
				continue;
			}
			double accepted = c.receiver().receive(delivered, c.face());
			if (accepted > 0) {
				return accepted;
			}
		}
		return 0;
	}

	/** 過圧でケーブルを蒸発させる (ドロップ無しで破壊演出付き). */
	private static void burnCable(Level level, BlockPos pos) {
		level.levelEvent(2001, pos, Block.getId(level.getBlockState(pos)));
		level.destroyBlock(pos, false);
	}
}
