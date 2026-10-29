package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.energy.MjPressure;

/** エンジン種別. 燃料形態・発電レート・出力パケットランクを規定する. */
public enum EngineKind {
	REDSTONE(FuelMode.NONE_POWERED, 0.2, MjPressure.ULV, true),
	STONE(FuelMode.SOLID, 1.0, MjPressure.LV, true),
	COMBUSTION(FuelMode.FLUID, 6.0, MjPressure.MV, true),
	CREATIVE(FuelMode.NONE_ALWAYS, 200.0, MjPressure.EV, false);

	/** 燃料形態: SOLID=固体燃料 / FLUID=流体燃料 / NONE_POWERED=燃料不要で RS 駆動 / NONE_ALWAYS=常時発電. */
	public enum FuelMode {
		SOLID, FLUID, NONE_POWERED, NONE_ALWAYS
	}

	private final FuelMode fuelMode;
	private final double rate;
	private final MjPressure outputRank;
	private final boolean addsEfp;

	EngineKind(FuelMode fuelMode, double rate, MjPressure outputRank, boolean addsEfp) {
		this.fuelMode = fuelMode;
		this.rate = rate;
		this.outputRank = outputRank;
		this.addsEfp = addsEfp;
	}

	public FuelMode fuelMode() {
		return fuelMode;
	}

	/** 流体燃料式か. */
	public boolean fluidFueled() {
		return fuelMode == FuelMode.FLUID;
	}

	/** レッドストーン信号が必要か (creative は不要). */
	public boolean needsPower() {
		return fuelMode != FuelMode.NONE_ALWAYS;
	}

	/** 発電レート (MJ/t). */
	public double rate() {
		return rate;
	}

	public MjPressure outputRank() {
		return outputRank;
	}

	/** 稼働中 EFP 加算対象か (creative デバッグ機は除外). */
	public boolean addsEfp() {
		return addsEfp;
	}
}
