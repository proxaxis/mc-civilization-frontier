package com.civilizationfrontier.bcn.item;

import java.util.function.Function;

import com.civilizationfrontier.bcn.Bcn;
import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.civilizationfrontier.bcn.fluid.BcnFluids;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class BcnItems {
	public static final Item COPPER_ORE = registerBlockItem("copper_ore", BcnBlocks.COPPER_ORE);
	public static final Item DEEPSLATE_COPPER_ORE = registerBlockItem("deepslate_copper_ore", BcnBlocks.DEEPSLATE_COPPER_ORE);
	public static final Item TIN_ORE = registerBlockItem("tin_ore", BcnBlocks.TIN_ORE);
	public static final Item DEEPSLATE_TIN_ORE = registerBlockItem("deepslate_tin_ore", BcnBlocks.DEEPSLATE_TIN_ORE);
	public static final Item SILVER_ORE = registerBlockItem("silver_ore", BcnBlocks.SILVER_ORE);
	public static final Item DEEPSLATE_SILVER_ORE = registerBlockItem("deepslate_silver_ore", BcnBlocks.DEEPSLATE_SILVER_ORE);
	public static final Item URANIUM_ORE = registerBlockItem("uranium_ore", BcnBlocks.URANIUM_ORE);
	public static final Item DEEPSLATE_URANIUM_ORE = registerBlockItem("deepslate_uranium_ore", BcnBlocks.DEEPSLATE_URANIUM_ORE);
	public static final Item RUBY_ORE = registerBlockItem("ruby_ore", BcnBlocks.RUBY_ORE);
	public static final Item DEEPSLATE_RUBY_ORE = registerBlockItem("deepslate_ruby_ore", BcnBlocks.DEEPSLATE_RUBY_ORE);
	public static final Item SAPPHIRE_ORE = registerBlockItem("sapphire_ore", BcnBlocks.SAPPHIRE_ORE);
	public static final Item DEEPSLATE_SAPPHIRE_ORE = registerBlockItem("deepslate_sapphire_ore", BcnBlocks.DEEPSLATE_SAPPHIRE_ORE);
	public static final Item PERIDOT_ORE = registerBlockItem("peridot_ore", BcnBlocks.PERIDOT_ORE);
	public static final Item DEEPSLATE_PERIDOT_ORE = registerBlockItem("deepslate_peridot_ore", BcnBlocks.DEEPSLATE_PERIDOT_ORE);
	public static final Item ELECTROTINE_ORE = registerBlockItem("electrotine_ore", BcnBlocks.ELECTROTINE_ORE);
	public static final Item DEEPSLATE_ELECTROTINE_ORE = registerBlockItem("deepslate_electrotine_ore", BcnBlocks.DEEPSLATE_ELECTROTINE_ORE);
	public static final Item MARBLE = registerBlockItem("marble", BcnBlocks.MARBLE);
	public static final Item OIL_SPRING = registerBlockItem("oil_spring", BcnBlocks.OIL_SPRING);
	public static final Item ENGINE_FRAME = registerBlockItem("engine_frame", BcnBlocks.ENGINE_FRAME);
	public static final Item ENGINE_STONE = registerBlockItem("engine_stone", BcnBlocks.ENGINE_STONE);
	public static final Item ITEM_PIPE = registerBlockItem("item_pipe", BcnBlocks.ITEM_PIPE);
	public static final Item WATERPROOF_PIPE = registerBlockItem("waterproof_pipe", BcnBlocks.WATERPROOF_PIPE);
	public static final Item PLANTER = registerBlockItem("planter", BcnBlocks.PLANTER);
	public static final Item MJ_SOURCE = registerBlockItem("mj_source", BcnBlocks.MJ_SOURCE);
	public static final Item CANNON = registerBlockItem("cannon", BcnBlocks.CANNON);
	public static final Item TREBUCHET = registerBlockItem("trebuchet", BcnBlocks.TREBUCHET);
	public static final Item CABLE_TIN = registerBlockItem("cable_tin", BcnBlocks.CABLE_TIN);
	public static final Item CABLE_COPPER = registerBlockItem("cable_copper", BcnBlocks.CABLE_COPPER);
	public static final Item CABLE_GOLD = registerBlockItem("cable_gold", BcnBlocks.CABLE_GOLD);
	public static final Item CABLE_GLASS_FIBER = registerBlockItem("cable_glass_fiber", BcnBlocks.CABLE_GLASS_FIBER);
	public static final Item CABLE_HV = registerBlockItem("cable_hv", BcnBlocks.CABLE_HV);
	public static final Item MJ_GENERATOR = registerBlockItem("mj_generator", BcnBlocks.MJ_GENERATOR);
	public static final Item MJ_CONSUMER = registerBlockItem("mj_consumer", BcnBlocks.MJ_CONSUMER);
	public static final Item TRANSFORMER_LV = registerBlockItem("transformer_lv", BcnBlocks.TRANSFORMER_LV);
	public static final Item TRANSFORMER_MV = registerBlockItem("transformer_mv", BcnBlocks.TRANSFORMER_MV);
	public static final Item TRANSFORMER_HV = registerBlockItem("transformer_hv", BcnBlocks.TRANSFORMER_HV);
	public static final Item BATTERY_BOX = registerBlockItem("battery_box", BcnBlocks.BATTERY_BOX);
	public static final Item MFE = registerBlockItem("mfe", BcnBlocks.MFE);
	public static final Item REDWIRE = registerBlockItem("redwire", BcnBlocks.REDWIRE);
	public static final Item GATE_NOT = registerBlockItem("gate_not", BcnBlocks.GATE_NOT);
	public static final Item GATE_BUFFER = registerBlockItem("gate_buffer", BcnBlocks.GATE_BUFFER);
	public static final Item GATE_OR = registerBlockItem("gate_or", BcnBlocks.GATE_OR);
	public static final Item GATE_NOR = registerBlockItem("gate_nor", BcnBlocks.GATE_NOR);
	public static final Item GATE_AND = registerBlockItem("gate_and", BcnBlocks.GATE_AND);
	public static final Item GATE_NAND = registerBlockItem("gate_nand", BcnBlocks.GATE_NAND);
	public static final Item GATE_XOR = registerBlockItem("gate_xor", BcnBlocks.GATE_XOR);
	public static final Item GATE_XNOR = registerBlockItem("gate_xnor", BcnBlocks.GATE_XNOR);
	public static final Item GATE_RS_LATCH = registerBlockItem("gate_rs_latch", BcnBlocks.GATE_RS_LATCH);
	public static final Item GATE_T_LATCH = registerBlockItem("gate_t_latch", BcnBlocks.GATE_T_LATCH);

	public static final Item RAW_TIN = register("raw_tin", Item::new, new Item.Properties());
	public static final Item RAW_SILVER = register("raw_silver", Item::new, new Item.Properties());
	public static final Item RAW_URANIUM = register("raw_uranium", Item::new, new Item.Properties());
	public static final Item TIN_INGOT = register("tin_ingot", Item::new, new Item.Properties());
	public static final Item SILVER_INGOT = register("silver_ingot", Item::new, new Item.Properties());
	public static final Item URANIUM_INGOT = register("uranium_ingot", Item::new, new Item.Properties());
	public static final Item RUBY = register("ruby", Item::new, new Item.Properties());
	public static final Item SAPPHIRE = register("sapphire", Item::new, new Item.Properties());
	public static final Item PERIDOT = register("peridot", Item::new, new Item.Properties());
	public static final Item ELECTROTINE_DUST = register("electrotine_dust", Item::new, new Item.Properties());
	public static final Item OIL_BUCKET = register("oil_bucket",
			properties -> new BucketItem(BcnFluids.OIL, properties),
			new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));
	public static final Item CANNONBALL = register("cannonball", Item::new, new Item.Properties());
	public static final Item SIEGE_SHOT = register("siege_shot", Item::new, new Item.Properties().stacksTo(16));

	private static Item registerBlockItem(String name, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Bcn.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key,
				new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Bcn.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
	}
}
