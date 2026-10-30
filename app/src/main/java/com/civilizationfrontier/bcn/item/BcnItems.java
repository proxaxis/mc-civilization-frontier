package com.civilizationfrontier.bcn.item;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.civilizationfrontier.bcn.Bcn;
import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.civilizationfrontier.bcn.lp.LpModule;
import com.civilizationfrontier.bcn.lp.LpModuleItem;
import com.civilizationfrontier.bcn.lp.RemoteOrdererItem;
import com.civilizationfrontier.bcn.fluid.BcnFluids;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
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
	public static final Item TANK = registerBlockItem("tank", BcnBlocks.TANK);
	public static final Item PUMP = registerBlockItem("pump", BcnBlocks.PUMP);
	public static final Item CHUTE = registerBlockItem("chute", BcnBlocks.CHUTE);
	public static final Item MINING_WELL = registerBlockItem("mining_well", BcnBlocks.MINING_WELL);
	public static final Item AUTO_WORKBENCH = registerBlockItem("auto_workbench", BcnBlocks.AUTO_WORKBENCH);
	public static final Item FILTERED_BUFFER = registerBlockItem("filtered_buffer", BcnBlocks.FILTERED_BUFFER);
	public static final Item FLOOD_GATE = registerBlockItem("flood_gate", BcnBlocks.FLOOD_GATE);
	public static final Item WATERPROOF_PIPE = registerBlockItem("waterproof_pipe", BcnBlocks.WATERPROOF_PIPE);
	public static final Item PLANTER = registerBlockItem("planter", BcnBlocks.PLANTER);
	public static final Item MJ_SOURCE = registerBlockItem("mj_source", BcnBlocks.MJ_SOURCE);
	public static final Item CANNON = registerBlockItem("cannon", BcnBlocks.CANNON);
	public static final Item TREBUCHET = registerBlockItem("trebuchet", BcnBlocks.TREBUCHET);
	public static final Item CABLE_TIN = registerBlockItem("cable_tin", BcnBlocks.CABLE_TIN);
	public static final Item CABLE_COPPER = registerBlockItem("cable_copper", BcnBlocks.CABLE_COPPER);
	public static final Item CABLE_GOLD = registerBlockItem("cable_gold", BcnBlocks.CABLE_GOLD);
	public static final Item CABLE_GOLD_2X = registerBlockItem("cable_gold_2x", BcnBlocks.CABLE_GOLD_2X);
	public static final Item CABLE_GOLD_4X = registerBlockItem("cable_gold_4x", BcnBlocks.CABLE_GOLD_4X);
	public static final Item CABLE_GLASS_FIBER = registerBlockItem("cable_glass_fiber", BcnBlocks.CABLE_GLASS_FIBER);
	public static final Item CABLE_HV = registerBlockItem("cable_hv", BcnBlocks.CABLE_HV);
	public static final Item CABLE_HV_2X = registerBlockItem("cable_hv_2x", BcnBlocks.CABLE_HV_2X);
	public static final Item CABLE_HV_4X = registerBlockItem("cable_hv_4x", BcnBlocks.CABLE_HV_4X);
	public static final Item CABLE_DETECTOR = registerBlockItem("cable_detector", BcnBlocks.CABLE_DETECTOR);
	public static final Item CABLE_SPLITTER = registerBlockItem("cable_splitter", BcnBlocks.CABLE_SPLITTER);
	public static final Item MJ_GENERATOR = registerBlockItem("mj_generator", BcnBlocks.MJ_GENERATOR);
	public static final Item MJ_CONSUMER = registerBlockItem("mj_consumer", BcnBlocks.MJ_CONSUMER);
	public static final Item TRANSFORMER_LV = registerBlockItem("transformer_lv", BcnBlocks.TRANSFORMER_LV);
	public static final Item TRANSFORMER_MV = registerBlockItem("transformer_mv", BcnBlocks.TRANSFORMER_MV);
	public static final Item TRANSFORMER_HV = registerBlockItem("transformer_hv", BcnBlocks.TRANSFORMER_HV);
	public static final Item BATTERY_BOX = registerBlockItem("battery_box", BcnBlocks.BATTERY_BOX);
	public static final Item BATBOX = registerBlockItem("batbox", BcnBlocks.BATBOX);
	public static final Item MFE = registerBlockItem("mfe", BcnBlocks.MFE);
	public static final Item MFSU = registerBlockItem("mfsu", BcnBlocks.MFSU);

	public static final Item RE_BATTERY = register("re_battery",
			properties -> new MjBatteryItem(4000, 13, true, 0x33CC33, properties), new Item.Properties());
	public static final Item SU_BATTERY = register("su_battery",
			properties -> new MjBatteryItem(400, 13, false, 0xCC3333, properties), new Item.Properties());
	public static final Item ENERGY_CRYSTAL = register("energy_crystal",
			properties -> new MjBatteryItem(400000, 51, true, 0x33CCCC, properties), new Item.Properties());
	public static final Item LAPOTRON_CRYSTAL = register("lapotron_crystal",
			properties -> new MjBatteryItem(4000000, 819, true, 0x3355CC, properties), new Item.Properties());
	public static final Item BATPACK = register("batpack",
			properties -> new MjBatteryItem(24000, 13, true, 0x33CC33, properties),
			new Item.Properties().equippable(net.minecraft.world.entity.EquipmentSlot.CHEST));
	public static final Item LAPPACK = register("lappack",
			properties -> new MjBatteryItem(120000, 51, true, 0x33CCCC, properties),
			new Item.Properties().equippable(net.minecraft.world.entity.EquipmentSlot.CHEST));
	public static final Item ENGINE_COMBUSTION = registerBlockItem("engine_combustion", BcnBlocks.ENGINE_COMBUSTION);
	public static final Item ENGINE_REDSTONE = registerBlockItem("engine_redstone", BcnBlocks.ENGINE_REDSTONE);
	public static final Item ASSEMBLY_TABLE = registerBlockItem("assembly_table", BcnBlocks.ASSEMBLY_TABLE);
	public static final Item LASER = registerBlockItem("laser", BcnBlocks.LASER);
	public static final Item INTEGRATION_TABLE = registerBlockItem("integration_table", BcnBlocks.INTEGRATION_TABLE);
	public static final Item CHARGING_TABLE = registerBlockItem("charging_table", BcnBlocks.CHARGING_TABLE);
	public static final Item GATE_COPIER = registerBlockItem("gate_copier", BcnBlocks.GATE_COPIER);
	public static final Item ADVANCED_CRAFTING_TABLE = registerBlockItem("advanced_crafting_table", BcnBlocks.ADVANCED_CRAFTING_TABLE);
	public static final Item DOCKING_STATION = registerBlockItem("docking_station", BcnBlocks.DOCKING_STATION);
	public static final Item CHIPSET_REDSTONE = register("chipset_redstone", Item::new, new Item.Properties());
	public static final Item CHIPSET_IRON = register("chipset_iron", Item::new, new Item.Properties());
	public static final Item CHIPSET_GOLD = register("chipset_gold", Item::new, new Item.Properties());
	public static final Item CHIPSET_QUARTZ = register("chipset_quartz", Item::new, new Item.Properties());
	public static final Item CHIPSET_DIAMOND = register("chipset_diamond", Item::new, new Item.Properties());
	public static final Item CHIPSET_PULSATING = register("chipset_pulsating", Item::new, new Item.Properties());
	public static final Item CHIPSET_COMP = register("chipset_comp", Item::new, new Item.Properties());
	public static final Item CHIPSET_EMERALD = register("chipset_emerald", Item::new, new Item.Properties());
	public static final Item GATE_IRON_AND = register("gate_iron_and", p -> new GateItem(GateItem.Kind.AND, p), new Item.Properties());
	public static final Item GATE_IRON_OR = register("gate_iron_or", p -> new GateItem(GateItem.Kind.OR, p), new Item.Properties());
	public static final Item GATE_GOLD_AND = register("gate_gold_and", p -> new GateItem(GateItem.Kind.AND, p), new Item.Properties());
	public static final Item GATE_GOLD_OR = register("gate_gold_or", p -> new GateItem(GateItem.Kind.OR, p), new Item.Properties());
	public static final Item GATE_DIAMOND_AND = register("gate_diamond_and", p -> new GateItem(GateItem.Kind.AND, p), new Item.Properties());
	public static final Item GATE_DIAMOND_OR = register("gate_diamond_or", p -> new GateItem(GateItem.Kind.OR, p), new Item.Properties());
	public static final Item GATE_EMERALD_AND = register("gate_emerald_and", p -> new GateItem(GateItem.Kind.AND, p), new Item.Properties());
	public static final Item GATE_EMERALD_OR = register("gate_emerald_or", p -> new GateItem(GateItem.Kind.OR, p), new Item.Properties());
	public static final Item GATE_QUARTZ_AND = register("gate_quartz_and", p -> new GateItem(GateItem.Kind.AND, p), new Item.Properties());
	public static final Item GATE_QUARTZ_OR = register("gate_quartz_or", p -> new GateItem(GateItem.Kind.OR, p), new Item.Properties());
	public static final Item GATE_MOD_CLOCK_TIMER = register("gate_mod_clock_timer", Item::new, new Item.Properties());
	public static final Item GATE_MOD_REDSTONE_FADER = register("gate_mod_redstone_fader", Item::new, new Item.Properties());
	public static final Item GATE_MOD_AUTARCHIC_PULSAR = register("gate_mod_autarchic_pulsar", Item::new, new Item.Properties());
	public static final Item GATE_MOD_LIGHT_SENSOR = register("gate_mod_light_sensor", Item::new, new Item.Properties());
	public static final Item ROBOT = register("robot", RobotItem::new, new Item.Properties());
	public static final Item REDSTONE_BOARD = register("redstone_board", Item::new, new Item.Properties());
	public static final Item REDSTONE_CRYSTAL = register("redstone_crystal", Item::new, new Item.Properties());
	public static final Item ENGINE_CREATIVE = registerBlockItem("engine_creative", BcnBlocks.ENGINE_CREATIVE);
	public static final Item REFINERY = registerBlockItem("refinery", BcnBlocks.REFINERY);
	public static final Item QUARRY = registerBlockItem("quarry", BcnBlocks.QUARRY);
	public static final Item FILLER = registerBlockItem("filler", BcnBlocks.FILLER);
	public static final Item BUILDER = registerBlockItem("builder", BcnBlocks.BUILDER);
	public static final Item FUEL_BUCKET = register("fuel_bucket",
			properties -> new BucketItem(BcnFluids.FUEL, properties),
			new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));
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
	public static final Item LANDMARK = registerBlockItem("landmark", BcnBlocks.LANDMARK);
	public static final Item PATH_MARK = registerBlockItem("path_mark", BcnBlocks.PATH_MARK);
	public static final Item ARCHITECT_TABLE = registerBlockItem("architect_table", BcnBlocks.ARCHITECT_TABLE);
	public static final Item TEMPLATE = register("template",
			p -> new com.civilizationfrontier.bcn.builder.SnapshotItem(p, com.civilizationfrontier.bcn.builder.SnapshotItem.Kind.TEMPLATE), new Item.Properties());
	public static final Item BLUEPRINT = register("blueprint",
			p -> new com.civilizationfrontier.bcn.builder.SnapshotItem(p, com.civilizationfrontier.bcn.builder.SnapshotItem.Kind.BLUEPRINT), new Item.Properties());
	public static final Item MAP_LOCATION = register("map_location",
			p -> new com.civilizationfrontier.bcn.builder.SnapshotItem(p, com.civilizationfrontier.bcn.builder.SnapshotItem.Kind.MAP_LOCATION), new Item.Properties());
	public static final Item LIST = register("list",
			p -> new com.civilizationfrontier.bcn.builder.SnapshotItem(p, com.civilizationfrontier.bcn.builder.SnapshotItem.Kind.LIST), new Item.Properties());

	public static final Item RAW_TIN = register("raw_tin", Item::new, new Item.Properties());
	public static final Item RAW_SILVER = register("raw_silver", Item::new, new Item.Properties());
	public static final Item RAW_URANIUM = register("raw_uranium", Item::new, new Item.Properties());
	public static final Item GENERATOR_GEOTHERMAL = registerBlockItem("generator_geothermal", BcnBlocks.GENERATOR_GEOTHERMAL);
	public static final Item GENERATOR_WATER = registerBlockItem("generator_water", BcnBlocks.GENERATOR_WATER);
	public static final Item GENERATOR_WIND = registerBlockItem("generator_wind", BcnBlocks.GENERATOR_WIND);
	public static final Item GENERATOR_SOLAR = registerBlockItem("generator_solar", BcnBlocks.GENERATOR_SOLAR);
	public static final Item REACTOR = registerBlockItem("reactor", BcnBlocks.REACTOR);
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
	public static final Item GEAR_WOOD = register("gear_wood", Item::new, new Item.Properties());
	public static final Item GEAR_STONE = register("gear_stone", Item::new, new Item.Properties());
	public static final Item GEAR_IRON = register("gear_iron", Item::new, new Item.Properties());
	public static final Item GEAR_GOLD = register("gear_gold", Item::new, new Item.Properties());
	public static final Item GEAR_DIAMOND = register("gear_diamond", Item::new, new Item.Properties());
	public static final Item WRENCH = register("wrench", BcnWrenchItem::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH = register("paintbrush", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_WHITE = register("paintbrush_white", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_ORANGE = register("paintbrush_orange", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_MAGENTA = register("paintbrush_magenta", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_LIGHT_BLUE = register("paintbrush_light_blue", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_YELLOW = register("paintbrush_yellow", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_LIME = register("paintbrush_lime", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_PINK = register("paintbrush_pink", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_GRAY = register("paintbrush_gray", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_LIGHT_GRAY = register("paintbrush_light_gray", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_CYAN = register("paintbrush_cyan", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_PURPLE = register("paintbrush_purple", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_BLUE = register("paintbrush_blue", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_BROWN = register("paintbrush_brown", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_GREEN = register("paintbrush_green", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_RED = register("paintbrush_red", Item::new, new Item.Properties().stacksTo(1));
	public static final Item PAINTBRUSH_BLACK = register("paintbrush_black", Item::new, new Item.Properties().stacksTo(1));
	public static final Item CANNONBALL = register("cannonball", Item::new, new Item.Properties());
	public static final Item SIEGE_SHOT = register("siege_shot", Item::new, new Item.Properties().stacksTo(16));
	public static final Item MACERATOR = registerBlockItem("macerator", BcnBlocks.MACERATOR);
	public static final Item EXTRACTOR = registerBlockItem("extractor", BcnBlocks.EXTRACTOR);
	public static final Item COMPRESSOR = registerBlockItem("compressor", BcnBlocks.COMPRESSOR);
	public static final Item CANNER = registerBlockItem("canner", BcnBlocks.CANNER);
	public static final Item ALLOY_SMELTER = registerBlockItem("alloy_smelter", BcnBlocks.ALLOY_SMELTER);
	public static final Item IRON_DUST = register("iron_dust", Item::new, new Item.Properties());
	public static final Item GOLD_DUST = register("gold_dust", Item::new, new Item.Properties());
	public static final Item COPPER_DUST = register("copper_dust", Item::new, new Item.Properties());
	public static final Item TIN_DUST = register("tin_dust", Item::new, new Item.Properties());
	public static final Item BRONZE_INGOT = register("bronze_ingot", Item::new, new Item.Properties());
	public static final Item STEEL_INGOT = register("steel_ingot", Item::new, new Item.Properties());
	public static final Item COAL_CHUNK = register("coal_chunk", Item::new, new Item.Properties());
	public static final Item RUBBER = register("rubber", Item::new, new Item.Properties());
	public static final Item TIN_CAN = register("tin_can", Item::new, new Item.Properties());
	public static final Item FUEL_CAN = register("fuel_can", Item::new, new Item.Properties().stacksTo(16));
	public static final Item CANNED_FOOD = register("canned_food", Item::new, new Item.Properties());

	public static final Item CROP_STICK = registerBlockItem("crop_stick", BcnBlocks.CROP_STICK);
	public static final Item BOOZE_BARREL = registerBlockItem("booze_barrel", BcnBlocks.BOOZE_BARREL);
	public static final Item CROP_ANALYZER = register("crop_analyzer", BcnAnalyzerItem::new,
			new Item.Properties().stacksTo(1));
	public static final Item CROP_SEED_BAG = register("crop_seed_bag", BcnSeedBagItem::new,
			new Item.Properties().stacksTo(16));
	public static final Item FERTILIZER = register("fertilizer", Item::new, new Item.Properties());
	public static final Item HYDRATED_COAL = register("hydrated_coal", Item::new, new Item.Properties());
	public static final Item WEED_EX = register("weed_ex", Item::new, new Item.Properties());
	public static final Item GRIN_POWDER = register("grin_powder", Item::new, new Item.Properties());
	public static final Item HOPS = register("hops", Item::new, new Item.Properties());
	public static final Item COFFEE_BEANS = register("coffee_beans", Item::new, new Item.Properties());
	public static final Item COFFEE_POWDER = register("coffee_powder", Item::new, new Item.Properties());
	public static final Item STONE_MUG = register("stone_mug", Item::new, new Item.Properties().stacksTo(16));
	public static final Item MUG_RUM = registerMug("mug_rum",
			effect(MobEffects.RESISTANCE, 1200, 0, 0.25F),
			effect(MobEffects.STRENGTH, 1200, 0, 0.25F),
			effect(MobEffects.NAUSEA, 600, 0, 0.25F),
			effect(MobEffects.POISON, 300, 0, 0.25F));
	public static final Item MUG_RUM_AGED = registerMug("mug_rum_aged",
			effect(MobEffects.RESISTANCE, 2400, 1, 1.0F),
			effect(MobEffects.FIRE_RESISTANCE, 2400, 0, 1.0F),
			effect(MobEffects.STRENGTH, 1200, 0, 1.0F),
			effect(MobEffects.NAUSEA, 600, 0, 1.0F));
	public static final Item MUG_YOUNGSTER = registerMug("mug_youngster",
			effect(MobEffects.SPEED, 600, 0, 1.0F));
	public static final Item MUG_BEER = registerMug("mug_beer",
			effect(MobEffects.SPEED, 1200, 0, 1.0F),
			effect(MobEffects.HASTE, 1200, 0, 1.0F),
			effect(MobEffects.NAUSEA, 300, 0, 1.0F));
	public static final Item MUG_ALE = registerMug("mug_ale",
			effect(MobEffects.SPEED, 2400, 1, 1.0F),
			effect(MobEffects.HASTE, 2400, 1, 1.0F),
			effect(MobEffects.STRENGTH, 1200, 0, 1.0F),
			effect(MobEffects.NAUSEA, 600, 0, 1.0F),
			effect(MobEffects.HUNGER, 300, 0, 1.0F));
	public static final Item MUG_BLACK_STUFF = registerMug("mug_black_stuff",
			effect(MobEffects.POISON, 400, 0, 0.3F),
			effect(MobEffects.NAUSEA, 600, 0, 0.3F),
			effect(MobEffects.BLINDNESS, 400, 0, 0.3F),
			effect(MobEffects.HUNGER, 400, 0, 0.3F),
			effect(MobEffects.WEAKNESS, 600, 0, 0.3F));
	public static final Item MUG_COLD_COFFEE = registerMug("mug_cold_coffee",
			effect(MobEffects.SPEED, 400, 0, 1.0F));
	public static final Item MUG_DARK_COFFEE = registerMug("mug_dark_coffee",
			effect(MobEffects.SPEED, 1200, 0, 1.0F),
			effect(MobEffects.HASTE, 1200, 0, 1.0F),
			new RemoveStatusEffectsConsumeEffect(
					HolderSet.direct(List.of(MobEffects.SLOWNESS, MobEffects.MINING_FATIGUE))));
	public static final Item MUG_COFFEE = registerMug("mug_coffee",
			effect(MobEffects.SPEED, 2400, 1, 1.0F),
			effect(MobEffects.HASTE, 2400, 1, 1.0F),
			effect(MobEffects.JUMP_BOOST, 2400, 0, 1.0F),
			new RemoveStatusEffectsConsumeEffect(
					HolderSet.direct(List.of(MobEffects.SLOWNESS, MobEffects.MINING_FATIGUE))));

	// ---- Project:Red 系 ----

	public static final Map<net.minecraft.world.item.DyeColor, Item> INSULATED_WIRE = registerInsulatedWireItems();
	public static final Item BUNDLED_CABLE = registerBlockItem("bundled_cable", BcnBlocks.BUNDLED_CABLE);
	public static final Item IC_GATE = registerBlockItem("ic_gate", BcnBlocks.IC_GATE);
	public static final Item FRAME = registerBlockItem("frame", BcnBlocks.FRAME);
	public static final Item FRAME_MOTOR = registerBlockItem("frame_motor", BcnBlocks.FRAME_MOTOR);
	public static final Item MICROBLOCK = register("microblock",
			p -> new MicroblockItem(BcnBlocks.MICROBLOCK, p), new Item.Properties());
	public static final Item BOULE = registerBlockItem("boule", BcnBlocks.BOULE);
	public static final Item BT_BATTERY_BOX = registerBlockItem("bt_battery_box", BcnBlocks.BT_BATTERY_BOX);
	public static final Item SOLAR_PANEL = registerBlockItem("solar_panel", BcnBlocks.SOLAR_PANEL);

	public static final Item PLATE = register("plate", Item::new, new Item.Properties());
	public static final Item CONDUCTIVE_PLATE = register("conductive_plate", Item::new, new Item.Properties());
	public static final Item WIRED_PLATE = register("wired_plate", Item::new, new Item.Properties());
	public static final Item BUNDLED_PLATE = register("bundled_plate", Item::new, new Item.Properties());
	public static final Item ANODE = register("anode", Item::new, new Item.Properties());
	public static final Item CATHODE = register("cathode", Item::new, new Item.Properties());
	public static final Item POINTER = register("pointer", Item::new, new Item.Properties());
	public static final Item SILICON_CHIP = register("silicon_chip", Item::new, new Item.Properties());
	public static final Item ENERGIZED_SILICON_CHIP = register("energized_silicon_chip", Item::new, new Item.Properties());
	public static final Item PLATE_ASSEMBLY = register("plate_assembly", Item::new, new Item.Properties());
	public static final Item RED_INGOT = register("red_ingot", Item::new, new Item.Properties());
	public static final Item SILICON = register("silicon", Item::new, new Item.Properties());
	public static final Item INFUSED_SILICON = register("infused_silicon", Item::new, new Item.Properties());
	public static final Item ENERGIZED_SILICON = register("energized_silicon", Item::new, new Item.Properties());
	public static final Item ELECTRO_SILICON = register("electro_silicon", Item::new, new Item.Properties());
	public static final Item MOTOR = register("motor", Item::new, new Item.Properties());
	public static final Item COPPER_COIL = register("copper_coil", Item::new, new Item.Properties());
	public static final Item IRON_COIL = register("iron_coil", Item::new, new Item.Properties());
	public static final Item GOLD_COIL = register("gold_coil", Item::new, new Item.Properties());
	public static final Item WOVEN_CLOTH = register("woven_cloth", Item::new, new Item.Properties());
	public static final Item SAIL = register("sail", Item::new, new Item.Properties());
	public static final Item RED_IRON_COMP = register("red_iron_comp", Item::new, new Item.Properties());
	public static final Item SAND_COAL_COMP = register("sand_coal_comp", Item::new, new Item.Properties());
	public static final Item RED_SILICON_COMP = register("red_silicon_comp", Item::new, new Item.Properties());
	public static final Item GLOW_SILICON_COMP = register("glow_silicon_comp", Item::new, new Item.Properties());
	public static final Item ELECTROTINE_INGOT = register("electrotine_ingot", Item::new, new Item.Properties());
	public static final Item ELECTROTINE_IRON_COMP = register("electrotine_iron_comp", Item::new, new Item.Properties());
	public static final Item ELECTROTINE_SILICON_COMP = register("electrotine_silicon_comp", Item::new, new Item.Properties());
	public static final Item SCREWDRIVER = register("screwdriver", Item::new, new Item.Properties().stacksTo(1));
	public static final Item DRAW_PLATE = register("draw_plate", Item::new, new Item.Properties().stacksTo(1));
	public static final Item MULTIMETER = register("multimeter", BcnMultimeterItem::new, new Item.Properties());
	public static final Item HANDSAW = register("handsaw",
			p -> new BcnHandsawItem(false, p), new Item.Properties());
	public static final Item DIAMOND_HANDSAW = register("diamond_handsaw",
			p -> new BcnHandsawItem(true, p), new Item.Properties());
	public static final Item RUBY_HANDSAW = register("ruby_handsaw",
			p -> new BcnHandsawItem(true, p), new Item.Properties());
	public static final Item SAPPHIRE_HANDSAW = register("sapphire_handsaw",
			p -> new BcnHandsawItem(true, p), new Item.Properties());
	public static final Item PERIDOT_HANDSAW = register("peridot_handsaw",
			p -> new BcnHandsawItem(true, p), new Item.Properties());
	public static final Item BT_BATTERY = register("bt_battery",
			p -> new MjBatteryItem(24000, 64, true, 0x3b6fff, p), new Item.Properties());

	public static final Item PIPE_BASIC_LOGISTICS = registerBlockItem("pipe_basic_logistics", BcnBlocks.PIPE_BASIC_LOGISTICS);
	public static final Item PIPE_PROVIDER = registerBlockItem("pipe_provider", BcnBlocks.PIPE_PROVIDER);
	public static final Item PIPE_REQUEST = registerBlockItem("pipe_request", BcnBlocks.PIPE_REQUEST);
	public static final Item PIPE_SUPPLIER = registerBlockItem("pipe_supplier", BcnBlocks.PIPE_SUPPLIER);
	public static final Item PIPE_CRAFTING = registerBlockItem("pipe_crafting", BcnBlocks.PIPE_CRAFTING);
	public static final Item PIPE_CHASSIS_MK1 = registerBlockItem("pipe_chassis_mk1", BcnBlocks.PIPE_CHASSIS_MK1);
	public static final Item PIPE_CHASSIS_MK2 = registerBlockItem("pipe_chassis_mk2", BcnBlocks.PIPE_CHASSIS_MK2);
	public static final Item PIPE_CHASSIS_MK3 = registerBlockItem("pipe_chassis_mk3", BcnBlocks.PIPE_CHASSIS_MK3);
	public static final Item PIPE_CHASSIS_MK4 = registerBlockItem("pipe_chassis_mk4", BcnBlocks.PIPE_CHASSIS_MK4);
	public static final Item PIPE_CHASSIS_MK5 = registerBlockItem("pipe_chassis_mk5", BcnBlocks.PIPE_CHASSIS_MK5);
	public static final Item PIPE_LIQUID_SUPPLIER = registerBlockItem("pipe_liquid_supplier", BcnBlocks.PIPE_LIQUID_SUPPLIER);
	public static final Item SOLDERING_STATION = registerBlockItem("soldering_station", BcnBlocks.SOLDERING_STATION);

	public static final Item MODULE_BLANK = register("module_blank",
			p -> new LpModuleItem(LpModule.BLANK, p), new Item.Properties());
	public static final Item MODULE_ITEM_SINK = register("module_item_sink",
			p -> new LpModuleItem(LpModule.ITEM_SINK, p), new Item.Properties());
	public static final Item MODULE_POLYMORPHIC = register("module_polymorphic",
			p -> new LpModuleItem(LpModule.POLYMORPHIC_SINK, p), new Item.Properties());
	public static final Item MODULE_TERMINUS = register("module_terminus",
			p -> new LpModuleItem(LpModule.TERMINUS_SINK, p), new Item.Properties());
	public static final Item MODULE_PASSIVE_SUPPLIER = register("module_passive_supplier",
			p -> new LpModuleItem(LpModule.PASSIVE_SUPPLIER, p), new Item.Properties());
	public static final Item MODULE_PROVIDER = register("module_provider",
			p -> new LpModuleItem(LpModule.PROVIDER, p), new Item.Properties());
	public static final Item MODULE_EXTRACTOR = register("module_extractor",
			p -> new LpModuleItem(LpModule.EXTRACTOR, p), new Item.Properties());
	public static final Item MODULE_QUICK_SORT = register("module_quick_sort",
			p -> new LpModuleItem(LpModule.QUICK_SORT, p), new Item.Properties());
	public static final Item MODULE_ACTIVE_SUPPLIER = register("module_active_supplier",
			p -> new LpModuleItem(LpModule.ACTIVE_SUPPLIER, p), new Item.Properties());
	public static final Item MODULE_CRAFTING = register("module_crafting",
			p -> new LpModuleItem(LpModule.CRAFTING, p), new Item.Properties());
	public static final Item REMOTE_ORDERER = register("remote_orderer",
			RemoteOrdererItem::new, new Item.Properties().stacksTo(1));
	public static final Item NANO_HOPPER = register("nano_hopper", Item::new, new Item.Properties());
	public static final Item ITEM_CARD = register("item_card", Item::new, new Item.Properties());
	public static final Item HUD_BOW = register("hud_bow", Item::new, new Item.Properties());
	public static final Item HUD_GLASS = register("hud_glass", Item::new, new Item.Properties());
	public static final Item HUD_NOSE = register("hud_nose", Item::new, new Item.Properties());
	public static final Item HUD_GLASSES = register("hud_glasses", Item::new, new Item.Properties().stacksTo(1));

	private static Map<net.minecraft.world.item.DyeColor, Item> registerInsulatedWireItems() {
		Map<net.minecraft.world.item.DyeColor, Item> map = new EnumMap<>(net.minecraft.world.item.DyeColor.class);
		for (net.minecraft.world.item.DyeColor color : net.minecraft.world.item.DyeColor.values()) {
			map.put(color, registerBlockItem("insulated_wire_" + color.getName(),
					BcnBlocks.INSULATED_WIRE.get(color)));
		}
		return Map.copyOf(map);
	}

	private static Item registerBlockItem(String name, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Bcn.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key,
				new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix()));
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Bcn.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	/** 飲料マグの登録. 飲用で効果を付与し、空のマグカップが返る. */
	private static Item registerMug(String name, ConsumeEffect... effects) {
		Consumable.Builder builder = Consumables.defaultDrink().consumeSeconds(1.6F);
		for (ConsumeEffect effect : effects) builder.onConsume(effect);
		return register(name, Item::new, new Item.Properties().stacksTo(16)
				.usingConvertsTo(STONE_MUG)
				.component(DataComponents.CONSUMABLE, builder.build())
				.component(DataComponents.FOOD, new FoodProperties(0, 0.0F, true)));
	}

	private static ApplyStatusEffectsConsumeEffect effect(Holder<MobEffect> mobEffect, int ticks, int amplifier, float probability) {
		return new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(mobEffect, ticks, amplifier), probability);
	}

	public static void init() {
	}
}
