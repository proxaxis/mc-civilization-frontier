package com.civilizationfrontier.bcn.energy;

import net.minecraft.core.Direction;

/**
 * MJ パケットの受信側 (consumer/蓄電器/変圧器の入力面が実装).
 * BlockEntity が実装することを前提とし、ケーブル網の探索が到達点として拾う.
 */
public interface MjReceiver {

	/**
	 * パケットを受け取る. packetEnergy はケーブル損失を差し引いた到達量であり、
	 * その大きさ自体がパケット圧 (MJ-P) でもある.
	 *
	 * @param packetEnergy 到達パケットのエネルギー量 (MJ)
	 * @param face         パケットが入ってきた面 (受信ブロック基準)
	 * @return 実際に受け入れた量. 0 なら拒否 (別の経路/受信先が試される)
	 */
	double receive(double packetEnergy, Direction face);
}
