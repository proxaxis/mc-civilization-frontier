package com.civilizationfrontier.bcn;

import net.minecraft.resources.Identifier;

public final class Bcn {
	public static final String MOD_ID = "bcn";

	private Bcn() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
