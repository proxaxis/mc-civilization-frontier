package com.civilizationfrontier.bcn;

import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.energy.MjReceiver;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 開発・検証用コマンド.
 *
 * - `/bcn_mj <pos> <amount>`: 指定位置の {@link MjReceiver} へ MJ パケットを直接送る.
 *   発電機実装までの暫定給電経路としても使える.
 * - `/bcn_efp <amount>`: `cfb_efp` スコアボードへ直接加算する (EFP 連携の検証用).
 */
public final class BcnCommands {

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> register(dispatcher));
	}

	private static void register(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("bcn_mj")
				.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
				.then(Commands.argument("pos", BlockPosArgument.blockPos())
						.then(Commands.argument("amount", LongArgumentType.longArg(1))
								.executes(ctx -> feedMj(ctx.getSource(),
										BlockPosArgument.getLoadedBlockPos(ctx, "pos"),
										LongArgumentType.getLong(ctx, "amount"))))));
		d.register(Commands.literal("bcn_efp")
				.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
				.then(Commands.argument("amount", LongArgumentType.longArg())
						.executes(ctx -> addEfp(ctx.getSource(),
								LongArgumentType.getLong(ctx, "amount")))));
	}

	private static int feedMj(CommandSourceStack src, BlockPos pos, long amount) {
		BlockEntity be = src.getLevel().getBlockEntity(pos);
		if (!(be instanceof MjReceiver receiver)) {
			src.sendSystemMessage(Component.literal("no MJ receiver at " + pos.toShortString()));
			return 0;
		}
		// 受信側の許容パケット圧に合わせて分割給電する
		double fed = 0;
		long remaining = amount;
		while (remaining > 0) {
			double packet = Math.min(remaining, 51);
			double accepted = receiver.receive(packet, Direction.UP);
			if (accepted <= 0) break;
			fed += accepted;
			remaining -= accepted;
		}
		src.sendSystemMessage(Component.literal("fed " + fed + " MJ to " + pos.toShortString()));
		return (int) Math.min(fed, Integer.MAX_VALUE);
	}

	private static int addEfp(CommandSourceStack src, long amount) {
		long total = BcnEfp.add(src.getServer(), amount);
		src.sendSystemMessage(Component.literal("EFP +" + amount + " (total: " + total + ")"));
		return 1;
	}
}
