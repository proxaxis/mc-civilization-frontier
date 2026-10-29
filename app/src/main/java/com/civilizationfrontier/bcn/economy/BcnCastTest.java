package com.civilizationfrontier.bcn.economy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/** 検証用キャストコマンド: `/bcn_cast <player> <spell_id>` — Spell Engine の performSpell を直接呼ぶ. */
public final class BcnCastTest {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("bcn_cast")
            .then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("spell", StringArgumentType.greedyString())
                    .executes(ctx -> cast(ctx.getSource(),
                            EntityArgument.getPlayer(ctx, "player"),
                            StringArgumentType.getString(ctx, "spell"))))));
    }

    private static int cast(CommandSourceStack src, ServerPlayer p, String spellName) {
        Identifier spellId = Identifier.tryParse(spellName);
        if (spellId == null) { src.sendSystemMessage(Component.literal("bad id: " + spellName)); return 0; }
        try {
            Class<?> sci = Class.forName("net.spell_engine.internals.casting.SpellCastInteractor");
            src.sendSystemMessage(Component.literal("isInteractor=" + sci.isInstance(p)));
            Class<?> cr = Class.forName("net.spell_engine.network.Packets$CastRequest");
            Class<?> sn = Class.forName("net.spell_engine.network.ServerNetwork");
            Object req = null;
            for (java.lang.reflect.Constructor<?> c : cr.getDeclaredConstructors()) {
                c.setAccessible(true);
                try {
                    req = (c.getParameterCount() == 1)
                            ? c.newInstance(spellId)
                            : c.newInstance(spellId, null);
                    break;
                } catch (IllegalArgumentException ignored) {}
            }
            if (req == null) { src.sendSystemMessage(Component.literal("no ctor")); return 0; }
            Method h = sn.getDeclaredMethod("handleCastRequest", cr,
                    net.minecraft.server.MinecraftServer.class, ServerPlayer.class);
            h.setAccessible(true);
            h.invoke(null, req, p.level().getServer(), p);
            src.sendSystemMessage(Component.literal(p.getName().getString() + " cast " + spellId + " (fired)"));
            return 1;
        } catch (Throwable t) {
            src.sendSystemMessage(Component.literal("error: " + t));
        }
        return 0;
    }
}
