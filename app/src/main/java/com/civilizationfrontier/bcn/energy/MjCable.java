package com.civilizationfrontier.bcn.energy;

/**
 * MJ 伝送体 (ケーブル). 状態を持たずブロック自体が特性を持つ.
 * パケットのエネルギー量が maxPacket を超える経路ではケーブルが蒸発する.
 */
public interface MjCable {

	/** 許容パケット圧 (MJ-P). 超過パケットが侵入すると蒸発する. */
	double maxPacket();

	/** パケット1個が1ブロック通過するごとの減衰量 (MJ). IC2 源流値の 0.4 倍. */
	double lossPerBlock();
}
