package com.civilizationfrontier.bcn.client;

import java.util.List;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class ItemPipeRenderState extends BlockEntityRenderState {
	public record GhostItem(ItemStackRenderState item, float x, float y, float z) {
	}

	public List<GhostItem> items = List.of();
}
