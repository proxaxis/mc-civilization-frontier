package com.civilizationfrontier.bcn;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.client.ItemPipeRenderer;
import com.civilizationfrontier.bcn.entity.BcnEntities;
import com.civilizationfrontier.bcn.fluid.BcnFluids;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.resources.model.sprite.Material;

public final class BcnClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FluidModel.Unbaked oil = new FluidModel.Unbaked(
                new Material(Bcn.id("block/oil_still")),
                new Material(Bcn.id("block/oil_flow")),
                null, null);
        FluidRenderingRegistry.register(BcnFluids.OIL, BcnFluids.FLOWING_OIL, oil);

        EntityRendererRegistry.register(BcnEntities.CANNONBALL,
                context -> new ThrownItemRenderer<>(context, 1.0F, true));
        EntityRendererRegistry.register(BcnEntities.SIEGE_SHOT,
                context -> new ThrownItemRenderer<>(context, 2.0F, true));

        BlockEntityRenderers.register(MachineBlockEntities.ITEM_PIPE, ItemPipeRenderer::new);
    }
}
