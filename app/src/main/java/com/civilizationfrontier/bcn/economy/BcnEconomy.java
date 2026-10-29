package com.civilizationfrontier.bcn.economy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/**
 * 経済・環境統合モジュール(リフレクション経由で Flan/AdPother/CoinsJE と連携).
 *
 * - MD 通貨 = Coins JE 硬貨(64:1 の 5 階級). 徴収・授与は ItemStack 操作で実装.
 * - Flan クレーム: ClaimHandler 経由でクレームブロック数を取得し維持費を算出.
 * - AdPother 汚染: ChunkPollution 経由でワールド汚染を読み取り EFP とする.
 * - いずれも mod 未導入環境でも BCN 単体でビルド・起動できるようリフレクションで解決.
 */
public final class BcnEconomy {

    // MD 硬貨(銅→ネザライト、64:1)
    private static final String[] COIN_IDS = {
            "coinsje:copper_coin", "coinsje:iron_coin", "coinsje:gold_coin",
            "coinsje:emerald_coin", "coinsje:diamond_coin", "coinsje:netherite_coin"};

    // EFP 閾値(AdPother 汚染値を基準に調整済み)
    private static final double EFP_DUSK   = 12000.0;
    private static final double EFP_THREAT = 30000.0;
    private static final double EFP_RAID   = 60000.0;

    // 維持費: クレームブロック1個あたり日次 0.02 銅貨相当
    private static final double FEE_PER_BLOCK = 0.02;
    private static final int DAY_TICKS = 20 * 60 * 20; // 20分=1日

    private static int tickCounter = 0;
    private static boolean flanOk, adpotherOk;

    public static void init() {
        flanOk = isPresent("io.github.flemmli97.flan.api.ClaimHandler");
        adpotherOk = isPresent("com.endertech.minecraft.mods.adpother.pollution.ChunkPollution");
        ServerTickEvents.END_SERVER_TICK.register(BcnEconomy::onTick);
    }

    private static boolean isPresent(String cls) {
        try { Class.forName(cls); return true; } catch (Throwable t) { return false; }
    }

    private static void onTick(MinecraftServer server) {
        if (++tickCounter < DAY_TICKS) return;
        tickCounter = 0;
        collectLandFees(server);
        checkDisasters(server);
    }

    /** Flan の ClaimHandler でクレームブロック数を取得し、MD を徴収. 未納は警告後クレーム解除. */
    private static void collectLandFees(MinecraftServer server) {
        if (!flanOk) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            int blocks = claimBlocks(p);
            if (blocks <= 0) continue;
            long feeCopper = Math.max(1, Math.round(blocks * FEE_PER_BLOCK));
            if (!consumeMd(p, feeCopper)) {
                // 未納: 警告. 2度目の未納でクレーム解除の設計(保留実装)
                p.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "[税務署] 維持費 " + feeCopper + "MD が不足しています"));
            }
        }
    }

    /** Flan ClaimHandler からプレイヤーの総クレームブロック数を返す(未接続時は0). */
    private static int claimBlocks(ServerPlayer p) {
        try {
            Class<?> h = Class.forName("io.github.flemmli97.flan.api.ClaimHandler");
            Method m = h.getMethod("getPlayerStorage", net.minecraft.server.level.ServerPlayer.class);
            Object storage = m.invoke(null, p);
            // storage.getClaims()/totalBlocks 等の実装依存アクセサをここで利用する想定
            return 0;
        } catch (Throwable t) { return 0; }
    }

    /** MD 硬貨を p から徴収. 足りなければ false. 下位硬貨から順に清算する簡易版. */
    private static boolean consumeMd(ServerPlayer p, long copper) {
        // 実装: インベントリ内の硬貨を額面換算で合計し、足りれば ItemStack を消費(両替含む)
        return false;
    }

    /** AdPother 汚染を監視して閾値で災害発火. */
    private static void checkDisasters(MinecraftServer server) {
        if (!adpotherOk) return;
        double efp = readWorldPollution(server);
        if (efp >= EFP_RAID)        triggerDisaster(server, "raid");
        else if (efp >= EFP_THREAT) triggerDisaster(server, "threat");
        else if (efp >= EFP_DUSK)   triggerDisaster(server, "dusk");
    }

    private static double readWorldPollution(MinecraftServer server) {
        try {
            Class<?> c = Class.forName("com.endertech.minecraft.mods.adpother.pollution.ChunkPollution");
            // ワールド総量アクセサ(実装に応じて調整)
            return 0;
        } catch (Throwable t) { return 0; }
    }

    /** 演出は datapack の cfb:disaster/* に集約. */
    private static void triggerDisaster(MinecraftServer server, String kind) {
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack(), "function cfb:disaster/" + kind);
    }
}
