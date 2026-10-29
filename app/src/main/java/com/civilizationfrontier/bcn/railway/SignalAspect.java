package com.civilizationfrontier.bcn.railway;

import net.minecraft.util.StringRepresentable;

public enum SignalAspect implements StringRepresentable {
	CLEAR("clear"),
	STOP("stop");

	private final String name;

	SignalAspect(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
