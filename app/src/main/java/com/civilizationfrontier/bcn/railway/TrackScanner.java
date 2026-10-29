package com.civilizationfrontier.bcn.railway;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Rail graph utilities for block-section (閉塞) detection.
 *
 * A rail_signal post stands beside a "guard rail". The section is the set of
 * rails reachable from the guard rail on the section side, bounded by other
 * signal-guarded rails and a length cap.
 */
public final class TrackScanner {
	public static final int MAX_SECTION_RAILS = 128;

	private TrackScanner() {
	}

	public record Section(BlockPos guardRail, Direction entryDir, Set<Long> rails, AABB bounds) {
		public boolean contains(BlockPos pos) {
			return rails.contains(pos.asLong());
		}
	}

	/**
	 * The rail watched by a signal post. Prefer the right-hand side relative to
	 * the entry travel direction, fall back to the left-hand side.
	 */
	public static BlockPos findGuardRail(Level level, BlockPos signalPos, Direction facing) {
		BlockPos right = signalPos.relative(facing.getCounterClockWise());
		if (BaseRailBlock.isRail(level.getBlockState(right))) {
			return right;
		}
		BlockPos left = signalPos.relative(facing.getClockWise());
		if (BaseRailBlock.isRail(level.getBlockState(left))) {
			return left;
		}
		return null;
	}

	/**
	 * Compute the protected section behind the guard rail. The entry direction
	 * selects which branch of the guard rail belongs to the section.
	 */
	public static Section scan(Level level, BlockPos guardRail, Direction facing) {
		List<BlockPos> connections = connectionsOf(level, guardRail);
		Set<Long> rails = new HashSet<>();
		if (connections.isEmpty()) {
			return new Section(guardRail, facing, rails, new AABB(guardRail));
		}

		BlockPos inward = null;
		int best = 0;
		for (BlockPos c : connections) {
			int dot = (c.getX() - guardRail.getX()) * facing.getStepX()
					+ (c.getZ() - guardRail.getZ()) * facing.getStepZ();
			if (inward == null || dot > best) {
				inward = c;
				best = dot;
			}
		}

		Deque<BlockPos> queue = new ArrayDeque<>();
		if (best > 0) {
			queue.add(inward);
		} else {
			queue.addAll(connections);
		}

		while (!queue.isEmpty() && rails.size() < MAX_SECTION_RAILS) {
			BlockPos pos = queue.poll();
			if (!rails.add(pos.asLong())) {
				continue;
			}
			for (BlockPos next : connectionsOf(level, pos)) {
				if (rails.contains(next.asLong()) || next.equals(guardRail)) {
					continue;
				}
				if (isSignalGuarded(level, next)) {
					continue;
				}
				queue.add(next);
			}
		}
		return new Section(guardRail, facing, rails, boundsOf(guardRail, rails));
	}

	/** True when a signal post stands next to this rail (section boundary). */
	public static boolean isSignalGuarded(Level level, BlockPos railPos) {
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			if (level.getBlockState(railPos.relative(dir)).getBlock() instanceof RailSignalBlock) {
				return true;
			}
		}
		return false;
	}

	public static List<BlockPos> connectionsOf(Level level, BlockPos railPos) {
		BlockState state = level.getBlockState(railPos);
		if (!BaseRailBlock.isRail(state)) {
			return List.of();
		}
		return new RailState(level, railPos, state).getConnections();
	}

	/**
	 * Connection of the guard rail leading into the section, used to decide
	 * whether a cart on the guard rail is entering or leaving it.
	 */
	public static List<BlockPos> inwardConnections(Level level, Section section) {
		List<BlockPos> out = new ArrayList<>();
		for (BlockPos c : connectionsOf(level, section.guardRail())) {
			if (section.contains(c)) {
				out.add(c);
			}
		}
		if (out.isEmpty()) {
			out.addAll(connectionsOf(level, section.guardRail()));
		}
		return out;
	}

	private static AABB boundsOf(BlockPos guardRail, Set<Long> rails) {
		double minX = guardRail.getX(), minY = guardRail.getY(), minZ = guardRail.getZ();
		double maxX = minX + 1, maxY = minY + 1, maxZ = minZ + 1;
		for (long p : rails) {
			double x = BlockPos.getX(p), y = BlockPos.getY(p), z = BlockPos.getZ(p);
			minX = Math.min(minX, x);
			minY = Math.min(minY, y);
			minZ = Math.min(minZ, z);
			maxX = Math.max(maxX, x + 1);
			maxY = Math.max(maxY, y + 1);
			maxZ = Math.max(maxZ, z + 1);
		}
		return new AABB(minX, minY, minZ, maxX, maxY, maxZ).inflate(0.5, 1.5, 0.5);
	}
}
