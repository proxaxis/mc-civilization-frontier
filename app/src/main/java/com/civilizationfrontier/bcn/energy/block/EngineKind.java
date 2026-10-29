package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.energy.MjPressure;

/** エンジン種別. 燃料形態・発電レート・出力パケットランクを規定する. */
public enum EngineKind {
	STONE(1.0, MjPressure.LV, false),
	COMBUSTION(6.0, MjPressure.MV, true);

	private final double rate;
	private final MjPressure outputRank;
	private final boolean fluidFueled;

	EngineKind(double rate, MjPressure outputRank, boolean fluidFueled) {
		this.rate = rate;
		this.outputRank = outputRank;
		this.fluidFueled = fluidFueled;
	}

	/** 発電レート (MJ/t). */
	public double rate() {
		return rate;
	}

	public MjPressure outputRank() {
		return outputRank;
	}

	/** 流体燃料式か (false=固体燃料). */
	public boolean fluidFueled() {
		return fluidFueled;
	}
}
