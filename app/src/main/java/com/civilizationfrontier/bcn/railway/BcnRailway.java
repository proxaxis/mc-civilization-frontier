package com.civilizationfrontier.bcn.railway;

import com.civilizationfrontier.bcn.Bcn;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class BcnRailway {
	public static final RailSignalBlock RAIL_SIGNAL = registerBlock("rail_signal",
			RailSignalBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
					.mapColor(MapColor.COLOR_GRAY)
					.noOcclusion());
	public static final BlockEntityType<RailSignalBlockEntity> RAIL_SIGNAL_BE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, Bcn.id("rail_signal"),
			FabricBlockEntityTypeBuilder.create(RailSignalBlockEntity::new, RAIL_SIGNAL).build());
	public static final Item RAIL_SIGNAL_ITEM = registerItem("rail_signal", RAIL_SIGNAL);

	private static <T extends Block> T registerBlock(String name,
			java.util.function.Function<BlockBehaviour.Properties, T> factory,
			BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Bcn.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	private static Item registerItem(String name, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Bcn.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key,
				new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
	}

	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(TimetableManager::tickLevel);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				RailwayCommands.register(dispatcher));
	}
}
