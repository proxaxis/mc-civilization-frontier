package com.civilizationfrontier.bcn.railway;

import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RailwayCommands {
	private static final SuggestionProvider<CommandSourceStack> LINE_SUGGESTIONS = (ctx, builder) ->
			SharedSuggestionProvider.suggest(
					RailwayData.get(ctx.getSource().getServer()).lines.keySet(), builder);

	private RailwayCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("bcn")
				.then(Commands.literal("rail")
						.then(Commands.literal("line")
								.then(Commands.literal("create")
										.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
										.then(Commands.argument("name", StringArgumentType.word())
												.executes(RailwayCommands::lineCreate)
												.then(Commands.argument("period",
																IntegerArgumentType.integer(20))
														.executes(RailwayCommands::lineCreate))))
								.then(Commands.literal("delete")
										.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
										.then(Commands.argument("name", StringArgumentType.word())
												.suggests(LINE_SUGGESTIONS)
												.executes(RailwayCommands::lineDelete)))
								.then(Commands.literal("list").executes(RailwayCommands::lineList))
								.then(Commands.literal("info")
										.then(Commands.argument("name", StringArgumentType.word())
												.suggests(LINE_SUGGESTIONS)
												.executes(RailwayCommands::lineInfo))))
						.then(Commands.literal("stop")
								.then(Commands.literal("add")
										.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
										.then(Commands.argument("line", StringArgumentType.word())
												.suggests(LINE_SUGGESTIONS)
												.then(Commands.literal("at")
														.then(Commands.argument("offset",
																		IntegerArgumentType.integer(0))
																.executes(ctx -> stopAdd(ctx, TrainLine.Mode.AT, null))
																.then(Commands.argument("pos",
																				BlockPosArgument.blockPos())
																		.executes(ctx -> stopAdd(ctx,
																				TrainLine.Mode.AT,
																				BlockPosArgument.getBlockPos(ctx, "pos"))))))
												.then(Commands.literal("dwell")
														.then(Commands.argument("ticks",
																		IntegerArgumentType.integer(1))
																.executes(ctx -> stopAdd(ctx, TrainLine.Mode.DWELL, null))
																.then(Commands.argument("pos",
																				BlockPosArgument.blockPos())
																		.executes(ctx -> stopAdd(ctx,
																				TrainLine.Mode.DWELL,
																				BlockPosArgument.getBlockPos(ctx, "pos"))))))))
								.then(Commands.literal("remove")
										.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
										.then(Commands.argument("line", StringArgumentType.word())
												.suggests(LINE_SUGGESTIONS)
												.then(Commands.argument("index",
																IntegerArgumentType.integer(0))
														.executes(RailwayCommands::stopRemove)))))
						.then(Commands.literal("assign")
								.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
								.then(Commands.argument("line", StringArgumentType.word())
										.suggests(LINE_SUGGESTIONS)
										.executes(RailwayCommands::assign)))
						.then(Commands.literal("unassign")
								.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
								.executes(RailwayCommands::unassign))
						.then(Commands.literal("trains").executes(RailwayCommands::trains))
						.then(Commands.literal("time").executes(RailwayCommands::time))));
	}

	private static int lineCreate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		String name = StringArgumentType.getString(ctx, "name");
		int period = TrainLine.DEFAULT_PERIOD;
		try {
			period = IntegerArgumentType.getInteger(ctx, "period");
		} catch (IllegalArgumentException ignored) {
		}
		RailwayData data = RailwayData.get(ctx.getSource().getServer());
		if (data.lines.containsKey(name)) {
			ctx.getSource().sendFailure(Component.literal("line already exists: " + name));
			return 0;
		}
		data.lines.put(name, new TrainLine(name, period));
		data.setDirty();
		int p = period;
		ctx.getSource().sendSuccess(() -> Component.literal("created line " + name + " period=" + p), true);
		return 1;
	}

	private static int lineDelete(CommandContext<CommandSourceStack> ctx) {
		String name = StringArgumentType.getString(ctx, "name");
		RailwayData data = RailwayData.get(ctx.getSource().getServer());
		if (data.lines.remove(name) == null) {
			ctx.getSource().sendFailure(Component.literal("no such line: " + name));
			return 0;
		}
		data.assignments.values().removeIf(name::equals);
		data.setDirty();
		ctx.getSource().sendSuccess(() -> Component.literal("deleted line " + name), true);
		return 1;
	}

	private static int lineList(CommandContext<CommandSourceStack> ctx) {
		RailwayData data = RailwayData.get(ctx.getSource().getServer());
		if (data.lines.isEmpty()) {
			ctx.getSource().sendSystemMessage(Component.literal("no lines defined"));
			return 0;
		}
		for (TrainLine line : data.lines.values()) {
			ctx.getSource().sendSystemMessage(Component.literal(
					line.name + " period=" + line.period + " stops=" + line.stops.size()));
		}
		return 1;
	}

	private static int lineInfo(CommandContext<CommandSourceStack> ctx) {
		String name = StringArgumentType.getString(ctx, "name");
		RailwayData data = RailwayData.get(ctx.getSource().getServer());
		TrainLine line = data.lines.get(name);
		if (line == null) {
			ctx.getSource().sendFailure(Component.literal("no such line: " + name));
			return 0;
		}
		ctx.getSource().sendSystemMessage(Component.literal(
				"line " + line.name + " period=" + line.period + " stops=" + line.stops.size()));
		for (int i = 0; i < line.stops.size(); i++) {
			TrainLine.Stop stop = line.stops.get(i);
			int idx = i;
			ctx.getSource().sendSystemMessage(Component.literal(
					"  [" + idx + "] " + stop.rail().toShortString() + " " + stop.mode().getSerializedName()
							+ " " + stop.value()));
		}
		return 1;
	}

	private static int stopAdd(CommandContext<CommandSourceStack> ctx, TrainLine.Mode mode, BlockPos pos) {
		String lineName = StringArgumentType.getString(ctx, "line");
		RailwayData data = RailwayData.get(ctx.getSource().getServer());
		TrainLine line = data.lines.get(lineName);
		if (line == null) {
			ctx.getSource().sendFailure(Component.literal("no such line: " + lineName));
			return 0;
		}
		int value = mode == TrainLine.Mode.AT
				? IntegerArgumentType.getInteger(ctx, "offset")
				: IntegerArgumentType.getInteger(ctx, "ticks");
		if (mode == TrainLine.Mode.AT && value >= line.period) {
			ctx.getSource().sendFailure(Component.literal("offset must be < period " + line.period));
			return 0;
		}
		BlockPos rail = pos;
		if (rail == null) {
			rail = findStationRail(ctx.getSource());
			if (rail == null) {
				ctx.getSource().sendFailure(Component.literal(
						"no station rail found nearby; pass <pos> or stand on the station rail"));
				return 0;
			}
		}
		line.stops.add(new TrainLine.Stop(rail, mode, value));
		data.setDirty();
		BlockPos r = rail;
		ctx.getSource().sendSuccess(() -> Component.literal(
				"added stop to " + lineName + ": " + r.toShortString() + " " + mode.getSerializedName()
						+ " " + value), true);
		return 1;
	}

	private static int stopRemove(CommandContext<CommandSourceStack> ctx) {
		String lineName = StringArgumentType.getString(ctx, "line");
		int index = IntegerArgumentType.getInteger(ctx, "index");
		RailwayData data = RailwayData.get(ctx.getSource().getServer());
		TrainLine line = data.lines.get(lineName);
		if (line == null) {
			ctx.getSource().sendFailure(Component.literal("no such line: " + lineName));
			return 0;
		}
		if (index >= line.stops.size()) {
			ctx.getSource().sendFailure(Component.literal("no stop at index " + index));
			return 0;
		}
		TrainLine.Stop removed = line.stops.remove(index);
		data.setDirty();
		ctx.getSource().sendSuccess(() -> Component.literal(
				"removed stop " + removed.rail().toShortString() + " from " + lineName), true);
		return 1;
	}

	private static int assign(CommandContext<CommandSourceStack> ctx) {
		String lineName = StringArgumentType.getString(ctx, "line");
		CommandSourceStack src = ctx.getSource();
		RailwayData data = RailwayData.get(src.getServer());
		if (!data.lines.containsKey(lineName)) {
			src.sendFailure(Component.literal("no such line: " + lineName));
			return 0;
		}
		AbstractMinecart head = nearestHead(src);
		if (head == null) {
			src.sendFailure(Component.literal("no managed train head within 10 blocks"));
			return 0;
		}
		int root = ImBridge.trainKey(head);
		data.assignments.put(root, lineName);
		data.setDirty();
		src.sendSuccess(() -> Component.literal("assigned train " + root + " to line " + lineName), true);
		return 1;
	}

	private static int unassign(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack src = ctx.getSource();
		AbstractMinecart head = nearestHead(src);
		if (head == null) {
			src.sendFailure(Component.literal("no managed train head within 10 blocks"));
			return 0;
		}
		RailwayData data = RailwayData.get(src.getServer());
		int root = ImBridge.trainKey(head);
		String removed = data.assignments.remove(root);
		data.setDirty();
		if (removed == null) {
			src.sendFailure(Component.literal("train " + root + " was not assigned"));
			return 0;
		}
		src.sendSuccess(() -> Component.literal("unassigned train " + root), true);
		return 1;
	}

	private static int trains(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack src = ctx.getSource();
		RailwayData data = RailwayData.get(src.getServer());
		int[] count = {0};
		for (AbstractMinecart cart : src.getLevel().getEntities(
				EntityTypeTest.forClass(AbstractMinecart.class),
				e -> e.entityTags().contains(ImBridge.TAG_MANAGED) && ImBridge.isHead(e))) {
			int root = ImBridge.trainKey(cart);
			Vec3 v = cart.getDeltaMovement();
			src.sendSystemMessage(Component.literal(String.format(
					"train %d pos=%s line=%s wait=%d zone=%d speed=%.2f",
					root, cart.blockPosition().toShortString(),
					data.assignments.getOrDefault(root, "-"),
					ImBridge.score(cart, ImBridge.OBJ_WAIT),
					ImBridge.score(cart, ImBridge.OBJ_ZONE),
					v.horizontalDistance())));
			count[0]++;
		}
		if (count[0] == 0) {
			src.sendSystemMessage(Component.literal("no managed trains in this dimension"));
		}
		return 1;
	}

	private static int time(CommandContext<CommandSourceStack> ctx) {
		long now = TimetableManager.clockTicks(ctx.getSource().getServer());
		ctx.getSource().sendSystemMessage(Component.literal(
				"clock=" + now + " day=" + (now / 24000) + " tod=" + (now % 24000)));
		return 1;
	}

	private static AbstractMinecart nearestHead(CommandSourceStack src) {
		Vec3 origin = src.getPosition();
		List<? extends AbstractMinecart> carts = src.getLevel().getEntities(
				EntityTypeTest.forClass(AbstractMinecart.class),
				new AABB(origin, origin).inflate(10),
				e -> e.entityTags().contains(ImBridge.TAG_MANAGED) && ImBridge.isHead(e));
		return carts.stream()
				.min(Comparator.comparingDouble(c -> c.distanceToSqr(origin)))
				.orElse(null);
	}

	/** Nearest rail with an Improvement Minecarts station block underneath. */
	private static BlockPos findStationRail(CommandSourceStack src) {
		ServerLevel level = src.getLevel();
		BlockPos center = BlockPos.containing(src.getPosition());
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -3, -8), center.offset(8, 3, 8))) {
			if (!BaseRailBlock.isRail(level.getBlockState(pos))) {
				continue;
			}
			var below = level.getBlockState(pos.below());
			boolean station = below.is(Blocks.LAPIS_BLOCK) || below.is(Blocks.EMERALD_BLOCK)
					|| below.is(Blocks.AMETHYST_BLOCK) || below.is(Blocks.GOLD_BLOCK)
					|| below.is(Blocks.HOPPER);
			if (!station) {
				continue;
			}
			double d = pos.distSqr(center);
			if (d < bestDist) {
				bestDist = d;
				best = pos.immutable();
			}
		}
		return best;
	}
}
