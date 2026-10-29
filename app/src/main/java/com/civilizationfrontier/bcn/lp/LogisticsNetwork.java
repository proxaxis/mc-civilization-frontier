package com.civilizationfrontier.bcn.lp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.civilizationfrontier.bcn.block.BcnPipeBlock;
import com.civilizationfrontier.bcn.block.entity.ItemPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

/**
 * 物流ネットワークの経路解決・宛先解決・要求発行を行う静的ユーティリティ.
 * 網状態を持たず、発送/配送の都度 BFS で探索する (MjNetwork と同じ方針).
 */
public final class LogisticsNetwork {

	/** BFS の最大探索パイプ数. */
	public static final int MAX_NODES = 256;
	/** destFace で宛先パイプ内部バッファへの直接配送を示す値. */
	public static final int FACE_INTERNAL = Direction.values().length;
	/** 宛先不明の送り先 (宛先パイプでラインアウト). */
	public static final int FACE_EJECT = -1;

	private LogisticsNetwork() {
	}

	/** 隣接コンテナとその接続面. */
	public record FaceContainer(Container container, Direction face) {
	}

	/** 無宛先アイテムの受け皿 (シンク). */
	public record Sink(BlockPos pipe, int face, int priority) {
	}

	/** start から到達可能なパイプ位置を BFS 順で列挙する. */
	public static List<BlockPos> members(Level level, BlockPos start) {
		return members(level, start, false);
	}

	public static boolean isItemPipe(Level level, BlockPos pos) {
		return level.getBlockState(pos).getBlock() instanceof BcnPipeBlock pipe && !pipe.fluid;
	}

	public static boolean isPipe(Level level, BlockPos pos, boolean fluid) {
		return level.getBlockState(pos).getBlock() instanceof BcnPipeBlock pipe && pipe.fluid == fluid;
	}

	/** from → to への最短経路 (各パイプでの出口方向列). 到達不能なら null. */
	public static List<Direction> route(Level level, BlockPos from, BlockPos to) {
		return route(level, from, to, false);
	}

	/** 液体系を含む経路解決. fluid=true で防水パイプ網を辿る. */
	public static List<Direction> route(Level level, BlockPos from, BlockPos to, boolean fluid) {
		if (from.equals(to)) {
			return List.of();
		}
		Map<BlockPos, BlockPos> parent = new HashMap<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		parent.put(from, null);
		queue.add(from);
		while (!queue.isEmpty() && parent.size() < MAX_NODES) {
			BlockPos cur = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos next = cur.relative(d);
				if (parent.containsKey(next) || !isPipe(level, next, fluid)) {
					continue;
				}
				parent.put(next, cur);
				if (next.equals(to)) {
					List<Direction> dirs = new ArrayList<>();
					for (BlockPos p = to, prev; (prev = parent.get(p)) != null; p = prev) {
						dirs.add(0, directionBetween(prev, p));
					}
					return dirs;
				}
				queue.add(next);
			}
		}
		return null;
	}

	/** start から到達可能なパイプ位置を BFS 順で列挙する (液体系は fluid=true). */
	public static List<BlockPos> members(Level level, BlockPos start, boolean fluid) {
		List<BlockPos> out = new ArrayList<>();
		Set<BlockPos> seen = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		seen.add(start);
		queue.add(start);
		while (!queue.isEmpty() && out.size() < MAX_NODES) {
			BlockPos cur = queue.poll();
			out.add(cur);
			for (Direction d : Direction.values()) {
				BlockPos next = cur.relative(d);
				if (seen.add(next) && isPipe(level, next, fluid)) {
					queue.add(next);
				}
			}
		}
		return out;
	}

	private static Direction directionBetween(BlockPos from, BlockPos to) {
		for (Direction d : Direction.values()) {
			if (from.relative(d).equals(to)) {
				return d;
			}
		}
		return Direction.NORTH;
	}

	/** パイプに隣接する最初のコンテナを返す (供給/回収の対象面). */
	public static FaceContainer attachedContainer(Level level, BlockPos pos) {
		for (Direction d : Direction.values()) {
			Container container = HopperBlockEntity.getContainerAt(level, pos.relative(d));
			if (container != null) {
				return new FaceContainer(container, d);
			}
		}
		return null;
	}

	/**
	 * 無宛先アイテムの受け皿を探す. 最も優先度の高いシンク (同順位は近い方) を返す.
	 */
	public static Sink resolveSink(Level level, BlockPos from, ItemStack stack) {
		Sink best = null;
		for (BlockPos pos : members(level, from)) {
			BlockEntity be = level.getBlockEntity(pos);
			if (!(be instanceof LogisticsPipeBlockEntity lp)) {
				continue;
			}
			Sink candidate = lp.sinkFor(stack);
			if (candidate != null && (best == null || candidate.priority() > best.priority())) {
				best = candidate;
			}
		}
		return best;
	}

	/**
	 * コンテナから指定アイテムを取出し、source のパイプから dest へ宛先付きで発送する.
	 *
	 * @return 発送できた個数
	 */
	public static int dispatch(Level level, BlockPos source, FaceContainer sourceInv, ItemStack want,
			BlockPos dest, int destFace) {
		if (!(level.getBlockEntity(source) instanceof ItemPipeBlockEntity pipe)) {
			return 0;
		}
		List<Direction> route = route(level, source, dest);
		if (route == null) {
			return 0;
		}
		Container container = sourceInv.container();
		int sent = 0;
		for (int slot = 0; slot < container.getContainerSize() && sent < want.getCount(); slot++) {
			ItemStack in = container.getItem(slot);
			if (!ItemStack.isSameItemSameComponents(in, want)) {
				continue;
			}
			int take = Math.min(in.getCount(), want.getCount() - sent);
			ItemStack taken = container.removeItem(slot, take);
			if (taken.isEmpty()) {
				continue;
			}
			if (pipe.addTransit(taken, sourceInv.face(), dest.asLong(), destFace, route)) {
				sent += taken.getCount();
			} else {
				// パイプが一杯ならコンテナへ戻す
				ItemStack rem = HopperBlockEntity.addItem(null, container, taken, sourceInv.face().getOpposite());
				if (!rem.isEmpty()) {
					net.minecraft.world.Containers.dropItemStack(level, source.getX() + 0.5,
							source.getY() + 0.5, source.getZ() + 0.5, rem);
				}
				break;
			}
		}
		return sent;
	}

	/**
	 * 網内の供給源から want を dest へ取り寄せる.
	 * 第1パスでプロバイダー在庫を、残りを第2パスで加工パイプのクラフトへ割り当てる.
	 */
	public static int request(Level level, ItemStack want, BlockPos searchFrom, BlockPos dest, int destFace) {
		List<BlockPos> members = members(level, searchFrom);
		int sent = 0;
		for (BlockPos pos : members) {
			if (sent >= want.getCount()) {
				break;
			}
			BlockEntity be = level.getBlockEntity(pos);
			if (!(be instanceof LogisticsPipeBlockEntity lp)) {
				continue;
			}
			FaceContainer provider = lp.providerSource();
			if (provider == null) {
				continue;
			}
			ItemStack part = want.copyWithCount(want.getCount() - sent);
			sent += dispatch(level, pos, provider, part, dest, destFace);
		}
		for (BlockPos pos : members) {
			if (sent >= want.getCount()) {
				break;
			}
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof LogisticsPipeBlockEntity lp
					&& lp.tryEnqueueCraft(want.copyWithCount(want.getCount() - sent), dest, destFace)) {
				sent = want.getCount();
			}
		}
		return sent;
	}
}
