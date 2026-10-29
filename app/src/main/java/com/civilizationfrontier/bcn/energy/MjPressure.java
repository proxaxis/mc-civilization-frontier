package com.civilizationfrontier.bcn.energy;

/**
 * MJ-P (パケット圧) ランク. 旧 IC2 EU-Packet の 0.4 倍換算 (TECH_ITEMS.md 0.1 節).
 * パケットのエネルギー量そのものが圧値として扱われ、ケーブル/機械の許容圧を超えると
 * ケーブルは蒸発、受信側は拒否する.
 */
public enum MjPressure {
	ULV(2, "超低圧"),
	LV(13, "低圧"),
	MV(51, "中圧"),
	HV(205, "高圧"),
	EV(819, "超高圧");

	private final int packetSize;
	private final String label;

	MjPressure(int packetSize, String label) {
		this.packetSize = packetSize;
		this.label = label;
	}

	/** ランクのパケットサイズ上限 (MJ). */
	public int packetSize() {
		return packetSize;
	}

	public String label() {
		return label;
	}
}
