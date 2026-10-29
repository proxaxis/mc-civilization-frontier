package com.civilizationfrontier.bcn.worldgen;

import com.civilizationfrontier.bcn.Bcn;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class BcnFeatures {
	public static void init() {
		register("oil_field", OilFieldFeature.CODEC);
	}

	private static void register(String name, MapCodec<? extends Feature> codec) {
		Registry.register(BuiltInRegistries.FEATURE_TYPE, Bcn.id(name), codec);
	}
}
