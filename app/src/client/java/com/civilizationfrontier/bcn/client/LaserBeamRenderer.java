package com.civilizationfrontier.bcn.client;

import java.util.List;

import com.civilizationfrontier.bcn.energy.block.entity.LaserBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** レーザービーム描画. 照射先の精密作業台へ赤い発光ラインを引く (最小実装). */
public class LaserBeamRenderer implements BlockEntityRenderer<LaserBlockEntity, LaserBeamRenderer.State> {

	public static class State extends BlockEntityRenderState {
		public List<BlockPos> targets = List.of();
		public BlockPos self = BlockPos.ZERO;
	}

	public LaserBeamRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(LaserBlockEntity be, State state, float partialTick,
			Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, overlay);
		state.targets = List.copyOf(be.targets());
		state.self = be.getBlockPos();
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector,
			CameraRenderState camera) {
		if (state.targets.isEmpty()) {
			return;
		}
		collector.submitCustomGeometry(pose, RenderTypes.lines(), (p, vc) -> {
			Matrix4f mat = p.pose();
			for (BlockPos tp : state.targets) {
				float dx = tp.getX() - state.self.getX() + 0.5F;
				float dy = tp.getY() - state.self.getY() + 0.5F;
				float dz = tp.getZ() - state.self.getZ() + 0.5F;
				vc.addVertex(mat, 0.5F, 0.5F, 0.5F).setColor(255, 60, 60, 255).setNormal(p, 0, 1, 0);
				vc.addVertex(mat, dx, dy, dz).setColor(255, 60, 60, 255).setNormal(p, 0, 1, 0);
			}
		});
	}
}
