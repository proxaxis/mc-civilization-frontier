package com.civilizationfrontier.bcn.energy;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * EFP (環境負荷指数) 連携 API.
 * 稼働中の機械が datapack 側の `cfb_efp` スコアボード (#efp ホルダ) へ
 * 直接加算するための連携口. 閾値判定・災害発火は datapack (cfb:disaster/*) 側の責務.
 */
public final class BcnEfp {

	public static final String OBJECTIVE = "cfb_efp";
	public static final String HOLDER = "#efp";

	private BcnEfp() {
	}

	/** EFP を加算 (負値で減算). 加算後の現在値を返す. */
	public static long add(MinecraftServer server, long amount) {
		ScoreAccess access = access(server);
		if (amount > Integer.MAX_VALUE) {
			amount = Integer.MAX_VALUE;
		} else if (amount < Integer.MIN_VALUE) {
			amount = Integer.MIN_VALUE;
		}
		access.add((int) amount);
		return access.get();
	}

	/** 現在の EFP 値を返す. */
	public static long get(MinecraftServer server) {
		return access(server).get();
	}

	private static ScoreAccess access(MinecraftServer server) {
		Scoreboard scoreboard = server.getScoreboard();
		Objective objective = scoreboard.getObjective(OBJECTIVE);
		if (objective == null) {
			objective = scoreboard.addObjective(OBJECTIVE, ObjectiveCriteria.DUMMY,
					Component.literal("EFP"), ObjectiveCriteria.RenderType.INTEGER, true, null);
		}
		return scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(HOLDER), objective);
	}
}
