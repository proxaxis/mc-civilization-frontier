package com.civilizationfrontier.bcn.railway;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.civilizationfrontier.bcn.Bcn;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Per-world timetable storage: lines, stops and train assignments. */
public final class RailwayData extends SavedData {
	public final Map<String, TrainLine> lines = new LinkedHashMap<>();
	/** improvement_minecarts train id (im.root) -> line name */
	public final Map<Integer, String> assignments = new HashMap<>();

	private static final Codec<RailwayData> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.unboundedMap(Codec.STRING, TrainLine.CODEC)
					.optionalFieldOf("lines", Map.of()).forGetter(d -> d.lines),
					Codec.compoundList(Codec.INT, Codec.STRING)
							.optionalFieldOf("assignments", List.of())
							.forGetter(d -> d.assignments.entrySet().stream()
									.map(e -> Pair.of(e.getKey(), e.getValue())).toList()))
			.apply(instance, RailwayData::new));

	public static final SavedDataType<RailwayData> TYPE = new SavedDataType<>(
			Bcn.id("railway"), RailwayData::new, CODEC, DataFixTypes.SAVED_DATA_WORLD_CLOCKS);

	private RailwayData() {
	}

	private RailwayData(Map<String, TrainLine> lines, List<Pair<Integer, String>> assignments) {
		this.lines.putAll(lines);
		for (Pair<Integer, String> entry : assignments) {
			this.assignments.put(entry.getFirst(), entry.getSecond());
		}
	}

	public static RailwayData get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public static RailwayData get(ServerLevel level) {
		return get(level.getServer());
	}
}
