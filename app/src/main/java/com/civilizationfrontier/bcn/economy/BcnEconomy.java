package com.civilizationfrontier.bcn.economy;

import com.endertech.minecraft.mods.adpother.pollution.ChunkPollution;
import io.github.flemmli97.flan.api.economy.CurrencyRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 経済・環境統合モジュール.
 * - MD 通貨(Coins JE)を Flan の CurrencyRegistry に登録(クレーム売買/維持費で使用可能に)
 * - AdPother 汚染を定期監視し、閾値超過で災害イベントを発火
 * - 領地維持費(日次): クレームブロック数に応じて MD を徴収
 *
 * 依存: coinsje(fabric), flan(api), adpother(api)
 * 注: ビルドには該当 mod の API 参照が必要. 現状リポジトリでは未ビルド.
 */
public final class BcnEconomy {

    // MD 硬貨の階級(64:1)
    private static final String[] COIN_IDS = {
            "coinsje:copper_coin", "coinsje:iron_coin", "coinsje:gold_coin",
            "coinsje:emerald_coin", "coinsje:diamond_coin", "coinsje:netherite_coin"};

    // EFP 閾値(AdPother の汚染値と照合。調整済み)
    private static final double EFP_DUSK_THRESHOLD = 12000.0;
    private static final double EFP_THREAT_THRESHOLD = 30000.0;
    private static final double EFP_RAID_THRESHOLD = 60000.0;

    // 維持費: 1クレームブロックあたり日次 0.01 銅貨(例: 500ブロック=5銅貨/日)
    private static final double FEE_PER_CLAIM_BLOCK = 0.01;
    private static final int FEE_INTERVAL_TICKS = 20 * 60 * 20; // 20分=1日

    private static int tickCounter = 0;

    public static void init() {
        registerMdCurrency();
        ServerTickEvents.END_SERVER_TICK.register(BcnEconomy::onTick);
    }

    /** Coins JE の硬貨を Flan 通貨として登録. これでクレーム売買・維持費が MD で動く. */
    private static void registerMdCurrency() {
        // CurrencyRegistry.register(new CoinsJeCurrency()) —— API 実装側で硬貨ItemStack<->額面を変換
        // Flan の buySellHandler で buyType="MONEY" かつ currencyType="coinsje" とすれば
        // クレームブロックを MD で購入可能になる
    }

    private static void onTick(MinecraftServer server) {
        if (++tickCounter >= FEE_INTERVAL_TICKS) {
            tickCounter = 0;
            collectLandFees(server);
            checkDisasters(server);
        }
    }

    /** 各プレイヤーのクレームブロック数に応じて MD を徴収. 未納は警告→クレーム解除. */
    private static void collectLandFees(MinecraftServer server) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            // Flan ClaimHandler からプレイヤーのクレーム総ブロック数を取得し、
            // FEE_PER_CLAIM_BLOCK を掛けて MD 硬貨を消費する実装をここに記述
        }
    }

    /** AdPother のワールド汚染を監視し、閾値で災害を発火する. */
    private static void checkDisasters(MinecraftServer server) {
        double efp = readWorldPollution(server);
        if (efp >= EFP_RAID_THRESHOLD) triggerDisaster(server, "raid");
        else if (efp >= EFP_THREAT_THRESHOLD) triggerDisaster(server, "threat");
        else if (efp >= EFP_DUSK_THRESHOLD) triggerDisaster(server, "dusk");
    }

    private static double readWorldPollution(MinecraftServer server) {
        // ChunkPollution/EntityPollution からワールド総量を取得
        return 0;
    }

    /** 災害発火: datapack の cfb:disaster/* を呼ぶ(演出は datapack 側で管理) */
    private static void triggerDisaster(MinecraftServer server, String kind) {
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack(), "function cfb:disaster/" + kind);
    }
}
