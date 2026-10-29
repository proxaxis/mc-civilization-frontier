package com.civilizationfrontier.bcn.railway;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;

/**
 * Bridge to Improvement Minecarts' scoreboard state. Improvement Minecarts is a
 * datapack-style mod; trains are exposed through scoreboard objectives on the
 * minecart entities themselves.
 */
public final class ImBridge {
	public static final String TAG_MANAGED = "im.managed";
	public static final String TAG_HEAD = "im.train";

	public static final String OBJ_ROOT = "im.root";
	public static final String OBJ_ORDER = "im.order";
	public static final String OBJ_WAIT = "im.wait";
	public static final String OBJ_ZONE = "im.zone";

	public static final int ZONE_NONE = 0;
	public static final int ZONE_STATION = 1;
	public static final int ZONE_CARGO = 2;
	public static final int ZONE_SIGNAL = 3;
	public static final int ZONE_TERMINAL = 4;

	private ImBridge() {
	}

	public static int score(Entity entity, String objectiveName) {
		Scoreboard scoreboard = entity.level().getScoreboard();
		Objective objective = scoreboard.getObjective(objectiveName);
		if (objective == null) {
			return 0;
		}
		ReadOnlyScoreInfo info = scoreboard.getPlayerScoreInfo(entity, objective);
		return info == null ? 0 : info.value();
	}

	public static void setScore(Entity entity, String objectiveName, int value) {
		Scoreboard scoreboard = entity.level().getScoreboard();
		Objective objective = scoreboard.getObjective(objectiveName);
		if (objective == null) {
			return;
		}
		scoreboard.getOrCreatePlayerScore(entity, objective).set(value);
	}

	public static boolean isManaged(Entity entity) {
		return entity.entityTags().contains(TAG_MANAGED);
	}

	/** The leader cart of a consist (im.order == 0), or any unmanaged cart. */
	public static boolean isHead(Entity entity) {
		return !isManaged(entity) || score(entity, OBJ_ORDER) == 0;
	}

	/** Identity of the consist a cart belongs to. */
	public static int trainKey(Entity entity) {
		if (!isManaged(entity)) {
			return entity.getId();
		}
		return score(entity, OBJ_ROOT);
	}
}
