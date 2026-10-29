package com.civilizationfrontier.bcn.block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.civilizationfrontier.bcn.block.entity.BundledCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 絶縁ワイヤー (色別) とケーブル束 (bundled cable) の信号評価.
 *
 * チャネル c のハイパーコンポーネントは「bundled cable 全体 ∪ 色 c の絶縁ワイヤー群」.
 * bundled cable は全 16 チャネルを同時に運ぶため、異なる色のワイヤーセグメントは
 * 同一ケーブル束を共有しても互いに混線しない.
 * チャネル内のいずれかの絶縁ワイヤーが外部ソースの信号を受けるとチャネル全体が
 * powered になり、bundled cable の BlockEntity にはチャネルビットが書き込まれる.
 *
 * redwire ({@link BcnWireBlock}) とは別ネットワークで相互に flood しない.
 * 絶縁ワイヤーの出力は redwire 側からは読める (弱信号) が、絶縁側は redwire を
 * ソースとして読まない (一方向ブリッジでフィードバックループを防ぐ).
 */
public final class ColoredWireNetwork {

	public static final int COLORS = 16;

	// 面6 + 辺12 (BcnWireBlock と同じ近傍モデル)
	private static final BlockPos[] NEIGHBOR_OFFSETS = buildNeighborOffsets();

	private static BlockPos[] buildNeighborOffsets() {
		List<BlockPos> list = new ArrayList<>();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					int m = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
					if (m == 1 || m == 2) {
						list.add(new BlockPos(dx, dy, dz));
					}
				}
			}
		}
		return list.toArray(new BlockPos[0]);
	}

	private ColoredWireNetwork() {
	}

	/**
	 * pos 周辺の絶縁ワイヤー・ケーブル束を種に、関連する全チャネルを再評価する.
	 * pos 自体がワイヤーでなくてもよい (撤去後の呼び出しに対応).
	 */
	public static void updateNetwork(Level level, BlockPos pos) {
		@SuppressWarnings("unchecked")
		Set<BlockPos>[] visited = new Set[COLORS];
		List<BlockPos> seeds = new ArrayList<>();
		seeds.add(pos);
		for (BlockPos off : NEIGHBOR_OFFSETS) {
			seeds.add(pos.offset(off.getX(), off.getY(), off.getZ()));
		}
		for (BlockPos seed : seeds) {
			Block sb = level.getBlockState(seed).getBlock();
			if (sb instanceof BcnBundledCableBlock) {
				for (int c = 0; c < COLORS; c++) {
					updateChannel(level, seed, c, visited);
				}
			} else if (sb instanceof BcnInsulatedWireBlock wire) {
				updateChannel(level, seed, wire.colorIndex(), visited);
			}
		}
	}

	/** チャネル color のハイパーコンポーネントを flood し powered を一括確定する. */
	private static void updateChannel(Level level, BlockPos seed, int color, Set<BlockPos>[] visited) {
		if (visited[color] == null) {
			visited[color] = new HashSet<>();
		}
		if (visited[color].contains(seed)) {
			return;
		}
		Set<BlockPos> members = new HashSet<>();
		Deque<BlockPos> open = new ArrayDeque<>();
		open.add(seed);
		while (!open.isEmpty()) {
			BlockPos p = open.poll();
			if (!members.add(p)) {
				continue;
			}
			visited[color].add(p);
			for (BlockPos off : NEIGHBOR_OFFSETS) {
				BlockPos q = p.offset(off.getX(), off.getY(), off.getZ());
				if (visited[color].contains(q) || members.contains(q)) {
					continue;
				}
				Block qb = level.getBlockState(q).getBlock();
				if (qb instanceof BcnBundledCableBlock
						|| (qb instanceof BcnInsulatedWireBlock w && w.colorIndex() == color)) {
					open.add(q);
				}
			}
		}

		boolean powered = false;
		for (BlockPos p : members) {
			if (level.getBlockState(p).getBlock() instanceof BcnInsulatedWireBlock
					&& externalSignal(level, p) > 0) {
				powered = true;
				break;
			}
		}
		for (BlockPos p : members) {
			BlockState s = level.getBlockState(p);
			if (s.getBlock() instanceof BcnInsulatedWireBlock) {
				if (s.getValue(BcnInsulatedWireBlock.POWERED) != powered) {
					level.setBlock(p, s.setValue(BcnInsulatedWireBlock.POWERED, powered), Block.UPDATE_ALL);
				}
			} else if (level.getBlockEntity(p) instanceof BundledCableBlockEntity be) {
				be.setChannel(color, powered);
			}
		}
	}

	/** 外部ソースの最大信号. ワイヤー系はループ防止のため読み対象外. */
	private static int externalSignal(Level level, BlockPos pos) {
		int signal = 0;
		for (Direction dir : Direction.values()) {
			BlockPos np = pos.relative(dir);
			BlockState ns = level.getBlockState(np);
			if (ns.getBlock() instanceof BcnWireBlock || ns.getBlock() instanceof BcnInsulatedWireBlock
					|| ns.getBlock() instanceof BcnBundledCableBlock
					|| ns.getBlock() instanceof RedstoneWireBlock) {
				continue;
			}
			signal = Math.max(signal, level.getSignal(np, dir));
			if (signal >= 15) {
				break;
			}
		}
		return signal;
	}
}
