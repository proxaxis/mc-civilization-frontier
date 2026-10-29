package com.civilizationfrontier.bcn.railway;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockInstance;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.entity.EntityTypeTest;

/**
 * Drives Improvement Minecarts' station dwell (im.wait) from the timetable.
 * While an assigned train is held at a scheduled station, its im.wait is pinned
 * to the remaining ticks until the scheduled departure; when the time comes the
 * score is released to 1 so the datapack departs it on its own path.
 */
public final class TimetableManager {
	private record Arrival(BlockPos pos, long gameTime) {
	}

	private static final Map<UUID, Arrival> arrivals = new HashMap<>();

	private TimetableManager() {
	}

	public static void tickLevel(ServerLevel level) {
		RailwayData data = RailwayData.get(level);
		if (data.assignments.isEmpty()) {
			arrivals.clear();
			return;
		}
		long now = clockTicks(level.getServer());
		long gameTime = level.getLevelData().getGameTime();
		Map<UUID, Boolean> seen = new HashMap<>();

		for (AbstractMinecart cart : level.getEntities(
				EntityTypeTest.forClass(AbstractMinecart.class),
				e -> e.entityTags().contains(ImBridge.TAG_MANAGED))) {
			if (ImBridge.score(cart, ImBridge.OBJ_ORDER) != 0) {
				continue;
			}
			if (ImBridge.score(cart, ImBridge.OBJ_WAIT) < 1) {
				continue;
			}
			int zone = ImBridge.score(cart, ImBridge.OBJ_ZONE);
			if (zone != ImBridge.ZONE_STATION && zone != ImBridge.ZONE_TERMINAL) {
				continue;
			}
			String lineName = data.assignments.get(ImBridge.trainKey(cart));
			if (lineName == null) {
				continue;
			}
			TrainLine line = data.lines.get(lineName);
			if (line == null) {
				continue;
			}
			TrainLine.Stop stop = line.stopAt(cart.blockPosition());
			if (stop == null) {
				continue;
			}
			long remaining = switch (stop.mode()) {
				case AT -> ticksUntilDeparture(line.period, stop.value(), now);
				case DWELL -> dwellRemaining(cart.getUUID(), cart.blockPosition(), gameTime, stop.value(), seen);
			};
			ImBridge.setScore(cart, ImBridge.OBJ_WAIT, (int) Math.max(remaining, 1));
		}

		arrivals.entrySet().removeIf(entry -> !seen.containsKey(entry.getKey()));
	}

	private static long dwellRemaining(UUID cartId, BlockPos pos, long gameTime, int dwell,
			Map<UUID, Boolean> seen) {
		Arrival arrival = arrivals.compute(cartId, (id, prev) -> {
			if (prev == null || !prev.pos().equals(pos)) {
				return new Arrival(pos, gameTime);
			}
			return prev;
		});
		seen.put(cartId, Boolean.TRUE);
		long remaining = arrival.pos().equals(pos) ? dwell - (gameTime - arrival.gameTime()) : dwell;
		if (remaining <= 0) {
			arrivals.remove(cartId);
		}
		return remaining;
	}

	private static long ticksUntilDeparture(int period, int offset, long now) {
		if (period <= 0) {
			return 0;
		}
		long inCycle = Math.floorMod(now, period);
		long target = offset - inCycle;
		if (target < 0) {
			target += period;
		}
		return target;
	}

	/** Ticks of the overworld world clock (the old getDayTime() equivalent). */
	public static long clockTicks(MinecraftServer server) {
		try {
			Holder<WorldClock> clock = server.registryAccess()
					.lookupOrThrow(Registries.WORLD_CLOCK)
					.getOrThrow(WorldClocks.OVERWORLD);
			ClockInstance instance = server.clockManager().getInstance(clock);
			if (instance != null) {
				return instance.totalTicks();
			}
		} catch (Throwable ignored) {
		}
		return server.overworld().getLevelData().getGameTime();
	}
}
