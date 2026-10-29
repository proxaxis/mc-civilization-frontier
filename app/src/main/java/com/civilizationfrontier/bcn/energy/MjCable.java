package com.civilizationfrontier.bcn.energy;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * MJ 伝送体 (ケーブル). 状態を持たずブロック自体が特性を持つ.
 * パケットのエネルギー量が maxPacket を超える経路ではケーブルが蒸発する.
 */
public interface MjCable {

	List<Direction> ALL_DIRECTIONS = List.of(Direction.values());

	/** 許容パケット圧 (MJ-P). 超過パケットが侵入すると蒸発する. */
	double maxPacket();

	/** パケット1個が1ブロック通過するごとの減衰量 (MJ). IC2 源流値の 0.4 倍. */
	double lossPerBlock();

	/**
	 * このケーブルを抜ける時の許容送出方向. 既定は全方向 (自由網).
	 * 分割ケーブルが入力面に応じて送出先を制限するために利用する.
	 *
	 * @param entry パケットがこのケーブルに入ってきた面 (ケーブル基準). null は送出元直結
	 */
	default List<Direction> routesFrom(Level level, BlockPos pos, Direction entry) {
		return ALL_DIRECTIONS;
	}

	/**
	 * パケットがこのケーブル位置を通過した時に呼ばれる (検出ケーブルの RS 出力用).
	 * 経路探索上の通過であり、到達可否・損失による消滅は問わない.
	 */
	default void onPacketPass(Level level, BlockPos pos, double packetEnergy) {
	}
}
