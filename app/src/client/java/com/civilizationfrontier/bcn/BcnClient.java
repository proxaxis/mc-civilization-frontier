package com.civilizationfrontier.bcn;

import com.civilizationfrontier.bcn.fluid.BcnFluids;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;

public final class BcnClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FluidModel.Unbaked oil = new FluidModel.Unbaked(
                new Material(Bcn.id("block/oil_still")),
                new Material(Bcn.id("block/oil_flow")),
                null, null);
        FluidRenderingRegistry.register(BcnFluids.OIL, BcnFluids.FLOWING_OIL, oil);
    }
}
