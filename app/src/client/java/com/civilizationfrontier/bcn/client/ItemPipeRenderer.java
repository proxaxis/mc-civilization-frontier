package com.civilizationfrontier.bcn.client;

import java.util.ArrayList;
import java.util.List;

import com.civilizationfrontier.bcn.block.entity.ItemPipeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public class ItemPipeRenderer implements BlockEntityRenderer<ItemPipeBlockEntity, ItemPipeRenderState> {

	private static final float SCALE = 0.375F;

	private final ItemModelResolver itemModelResolver;

	public ItemPipeRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public ItemPipeRenderState createRenderState() {
		return new ItemPipeRenderState();
	}

	@Override
	public void extractRenderState(ItemPipeBlockEntity be, ItemPipeRenderState state, float partialTick,
			Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);
		if (be.transit().isEmpty()) {
			state.items = List.of();
			return;
		}
		int seed = (int) be.getBlockPos().asLong();
		List<ItemPipeRenderState.GhostItem> items = new ArrayList<>(be.transit().size());
		for (ItemPipeBlockEntity.Transit t : be.transit()) {
			float progress = Math.min(t.progress + partialTick * ItemPipeBlockEntity.SPEED, 1.0F);
			Vec3 offset = progress <= 0.5F
					? t.from.getUnitVec3().scale(0.5 - progress)
					: t.to.getUnitVec3().scale(progress - 0.5);
			ItemStackRenderState item = new ItemStackRenderState();
			itemModelResolver.updateForTopItem(item, t.stack, ItemDisplayContext.FIXED, be.getLevel(), null, seed);
			items.add(new ItemPipeRenderState.GhostItem(item, (float) offset.x, (float) offset.y, (float) offset.z));
		}
		state.items = items;
	}

	@Override
	public void submit(ItemPipeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			CameraRenderState cameraRenderState) {
		for (ItemPipeRenderState.GhostItem ghost : state.items) {
			if (ghost.item().isEmpty()) {
				continue;
			}
			poseStack.pushPose();
			poseStack.translate(0.5F + ghost.x(), 0.5F + ghost.y(), 0.5F + ghost.z());
			poseStack.scale(SCALE, SCALE, SCALE);
			ghost.item().submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}
}
