package com.civilizationfrontier.bcn;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.client.mcp.McpHttpd;
import com.civilizationfrontier.bcn.client.ItemPipeRenderer;
import com.civilizationfrontier.bcn.client.LaserBeamRenderer;
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

        FluidModel.Unbaked fuel = new FluidModel.Unbaked(
                new Material(Bcn.id("block/fuel_still")),
                new Material(Bcn.id("block/fuel_flow")),
                null, null);
        FluidRenderingRegistry.register(BcnFluids.FUEL, BcnFluids.FLOWING_FUEL, fuel);

        EntityRendererRegistry.register(BcnEntities.CANNONBALL,
                context -> new ThrownItemRenderer<>(context, 1.0F, true));
        EntityRendererRegistry.register(BcnEntities.SIEGE_SHOT,
                context -> new ThrownItemRenderer<>(context, 2.0F, true));

        BlockEntityRenderers.register(MachineBlockEntities.ITEM_PIPE, ItemPipeRenderer::new);
        BlockEntityRenderers.register(MachineBlockEntities.LASER, LaserBeamRenderer::new);
        EntityRendererRegistry.register(BcnEntities.ROBOT,
                context -> new net.minecraft.client.renderer.entity.EntityRenderer<com.civilizationfrontier.bcn.entity.RobotEntity,
                        net.minecraft.client.renderer.entity.state.EntityRenderState>(context) {
                    @Override
                    public net.minecraft.client.renderer.entity.state.EntityRenderState createRenderState() {
                        return new net.minecraft.client.renderer.entity.state.EntityRenderState();
                    }
                });

        if (Boolean.parseBoolean(System.getProperty("bcn.mcp", "true"))) {
            McpHttpd.start();
        }
    }
}
