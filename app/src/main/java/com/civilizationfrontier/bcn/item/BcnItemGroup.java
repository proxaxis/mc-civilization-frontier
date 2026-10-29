package com.civilizationfrontier.bcn.item;

import com.civilizationfrontier.bcn.Bcn;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class BcnItemGroup {

	public static final CreativeModeTab GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
			Bcn.id("main"), FabricCreativeModeTab.builder()
					.icon(() -> new ItemStack(BcnItems.TIN_INGOT))
					.title(Component.translatable("itemGroup.bcn.main"))
					.displayItems((context, entries) -> {
						for (var item : BuiltInRegistries.ITEM) {
							Identifier id = BuiltInRegistries.ITEM.getKey(item);
							if (id != null && Bcn.MOD_ID.equals(id.getNamespace())) {
								entries.accept(item);
							}
						}
					})
					.build());

	public static void init() {
	}
}
