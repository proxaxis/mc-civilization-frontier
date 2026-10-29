package com.civilizationfrontier.bcn.fluid;

import com.civilizationfrontier.bcn.Bcn;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public final class BcnFluids {
	public static final FlowingFluid OIL = register("oil", new OilFluid.Still());
	public static final FlowingFluid FLOWING_OIL = register("flowing_oil", new OilFluid.Flowing());

	private static <T extends Fluid> T register(String name, T fluid) {
		return Registry.register(BuiltInRegistries.FLUID, Bcn.id(name), fluid);
	}

	public static void init() {
	}
}
