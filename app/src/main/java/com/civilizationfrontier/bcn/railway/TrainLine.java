package com.civilizationfrontier.bcn.railway;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;

/**
 * A cyclic timetable line. period is the cycle length in overworld clock ticks;
 * each stop either departs at a fixed offset in the cycle (AT) or after a fixed
 * dwell time (DWELL).
 */
public final class TrainLine {
	public enum Mode implements StringRepresentable {
		AT("at"), DWELL("dwell");

		public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

		private final String name;

		Mode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public record Stop(BlockPos rail, Mode mode, int value) {
		public static final Codec<Stop> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(BlockPos.CODEC.fieldOf("rail").forGetter(Stop::rail),
						Mode.CODEC.fieldOf("mode").forGetter(Stop::mode),
						Codec.INT.fieldOf("value").forGetter(Stop::value))
				.apply(instance, Stop::new));
	}

	public static final int DEFAULT_PERIOD = 2400;

	public final String name;
	public int period;
	public final List<Stop> stops = new ArrayList<>();

	public TrainLine(String name, int period) {
		this.name = name;
		this.period = period;
	}

	private TrainLine(String name, int period, List<Stop> stops) {
		this.name = name;
		this.period = period;
		this.stops.addAll(stops);
	}

	public static final Codec<TrainLine> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.STRING.fieldOf("name").forGetter(l -> l.name),
					Codec.INT.fieldOf("period").forGetter(l -> l.period),
					Codec.list(Stop.CODEC).fieldOf("stops").forGetter(l -> l.stops))
			.apply(instance, TrainLine::new));

	public Stop stopAt(BlockPos pos) {
		for (Stop stop : stops) {
			if (stop.rail().equals(pos)) {
				return stop;
			}
		}
		return null;
	}
}
