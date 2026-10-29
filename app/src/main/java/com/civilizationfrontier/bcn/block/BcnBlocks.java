package com.civilizationfrontier.bcn.block;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import com.civilizationfrontier.bcn.Bcn;
import com.civilizationfrontier.bcn.crop.BoozeBarrelBlock;
import com.civilizationfrontier.bcn.crop.CropStickBlock;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.EngineKind;
import com.civilizationfrontier.bcn.energy.block.MjBatteryBlock;
import com.civilizationfrontier.bcn.energy.block.MjCableBlock;
import com.civilizationfrontier.bcn.energy.block.MjCableDetectorBlock;
import com.civilizationfrontier.bcn.energy.block.MjCableSplitterBlock;
import com.civilizationfrontier.bcn.energy.block.MjConsumerBlock;
import com.civilizationfrontier.bcn.energy.block.Ic2GeneratorBlock;
import com.civilizationfrontier.bcn.energy.block.MjGeneratorBlock;
import com.civilizationfrontier.bcn.energy.block.MjQuarryBlock;
import com.civilizationfrontier.bcn.energy.block.MjRefineryBlock;
import com.civilizationfrontier.bcn.energy.block.MjTransformerBlock;
import com.civilizationfrontier.bcn.fluid.BcnFluids;
import com.civilizationfrontier.bcn.lp.LiquidSupplierPipeBlock;
import com.civilizationfrontier.bcn.lp.LogisticsPipeBlock;
import com.civilizationfrontier.bcn.lp.LpKind;
import com.civilizationfrontier.bcn.lp.SolderingStationBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneOreBlock;
import com.civilizationfrontier.bcn.energy.block.MjProcessorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class BcnBlocks {
	public static final Block COPPER_ORE = registerOre("copper_ore", Blocks.COPPER_ORE);
	public static final Block DEEPSLATE_COPPER_ORE = registerOre("deepslate_copper_ore", Blocks.DEEPSLATE_COPPER_ORE);
	public static final Block TIN_ORE = registerOre("tin_ore", Blocks.IRON_ORE);
	public static final Block DEEPSLATE_TIN_ORE = registerOre("deepslate_tin_ore", Blocks.DEEPSLATE_IRON_ORE);
	public static final Block SILVER_ORE = registerOre("silver_ore", Blocks.GOLD_ORE);
	public static final Block DEEPSLATE_SILVER_ORE = registerOre("deepslate_silver_ore", Blocks.DEEPSLATE_GOLD_ORE);
	public static final Block URANIUM_ORE = registerOre("uranium_ore", Blocks.IRON_ORE);
	public static final Block DEEPSLATE_URANIUM_ORE = registerOre("deepslate_uranium_ore", Blocks.DEEPSLATE_IRON_ORE);
	public static final Block RUBY_ORE = registerOre("ruby_ore", Blocks.DIAMOND_ORE);
	public static final Block DEEPSLATE_RUBY_ORE = registerOre("deepslate_ruby_ore", Blocks.DEEPSLATE_DIAMOND_ORE);
	public static final Block SAPPHIRE_ORE = registerOre("sapphire_ore", Blocks.DIAMOND_ORE);
	public static final Block DEEPSLATE_SAPPHIRE_ORE = registerOre("deepslate_sapphire_ore", Blocks.DEEPSLATE_DIAMOND_ORE);
	public static final Block PERIDOT_ORE = registerOre("peridot_ore", Blocks.DIAMOND_ORE);
	public static final Block DEEPSLATE_PERIDOT_ORE = registerOre("deepslate_peridot_ore", Blocks.DEEPSLATE_DIAMOND_ORE);
	public static final Block ELECTROTINE_ORE = register("electrotine_ore", RedStoneOreBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_ORE)
					.lightLevel(state -> state.getValue(RedStoneOreBlock.LIT) ? 9 : 0));
	public static final Block DEEPSLATE_ELECTROTINE_ORE = register("deepslate_electrotine_ore", RedStoneOreBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_REDSTONE_ORE)
					.lightLevel(state -> state.getValue(RedStoneOreBlock.LIT) ? 9 : 0));
	public static final Block MARBLE = register("marble", Block::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE).mapColor(MapColor.QUARTZ));
	public static final Block OIL = register("oil", properties -> new OilLiquidBlock(BcnFluids.OIL, properties),
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.replaceable()
					.noCollision()
					.strength(100.0F)
					.pushReaction(net.minecraft.world.level.material.PushReaction.POPPED)
					.liquid()
					.noLootTable());
	public static final Block ENGINE_FRAME = register("engine_frame", Block::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.METAL));
	public static final BcnEngineBlock ENGINE_STONE = register("engine_stone",
			properties -> new BcnEngineBlock(EngineKind.STONE, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.STONE));
	public static final BcnEngineBlock ENGINE_REDSTONE = register("engine_redstone",
			properties -> new BcnEngineBlock(EngineKind.REDSTONE, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).mapColor(MapColor.WOOD));
	public static final BcnEngineBlock ENGINE_CREATIVE = register("engine_creative",
			properties -> new BcnEngineBlock(EngineKind.CREATIVE, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_PURPLE));
	public static final BcnEngineBlock ENGINE_COMBUSTION = register("engine_combustion",
			properties -> new BcnEngineBlock(EngineKind.COMBUSTION, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.METAL)
					.requiresCorrectToolForDrops());
	public static final MjRefineryBlock REFINERY = register("refinery", MjRefineryBlock::new, machineProps());
	public static final MjQuarryBlock QUARRY = register("quarry", MjQuarryBlock::new, machineProps());
	public static final MjProcessorBlock MACERATOR = register("macerator",
			properties -> new MjProcessorBlock(properties, "macerator"), machineProps());
	public static final MjProcessorBlock EXTRACTOR = register("extractor",
			properties -> new MjProcessorBlock(properties, "extractor"), machineProps());
	public static final MjProcessorBlock COMPRESSOR = register("compressor",
			properties -> new MjProcessorBlock(properties, "compressor"), machineProps());
	public static final MjProcessorBlock CANNER = register("canner",
			properties -> new MjProcessorBlock(properties, "canner"), machineProps());
	public static final MjProcessorBlock ALLOY_SMELTER = register("alloy_smelter",
			properties -> new MjProcessorBlock(properties, "alloy_smelter"), machineProps());
	public static final BcnPipeBlock ITEM_PIPE = register("item_pipe",
			properties -> new BcnPipeBlock(properties, false),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final BcnTankBlock TANK = register("tank", BcnTankBlock::new,
			machineProps().noOcclusion());
	public static final BcnPumpBlock PUMP = register("pump", BcnPumpBlock::new, machineProps());
	public static final BcnChuteBlock CHUTE = register("chute", BcnChuteBlock::new, machineProps());
	public static final BcnMiningWellBlock MINING_WELL = register("mining_well", BcnMiningWellBlock::new, machineProps());
	public static final BcnSimpleMachineBlock AUTO_WORKBENCH = register("auto_workbench",
			p -> new BcnSimpleMachineBlock(p, BcnSimpleMachineBlock.Kind.AUTO_WORKBENCH), machineProps());
	public static final BcnSimpleMachineBlock FILTERED_BUFFER = register("filtered_buffer",
			p -> new BcnSimpleMachineBlock(p, BcnSimpleMachineBlock.Kind.FILTERED_BUFFER), machineProps());
	public static final BcnSimpleMachineBlock FLOOD_GATE = register("flood_gate",
			p -> new BcnSimpleMachineBlock(p, BcnSimpleMachineBlock.Kind.FLOOD_GATE), machineProps());
	public static final BcnPipeBlock WATERPROOF_PIPE = register("waterproof_pipe",
			properties -> new BcnPipeBlock(properties, true),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final BcnPlanterBlock PLANTER = register("planter", BcnPlanterBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_GREEN)
					.requiresCorrectToolForDrops());
	public static final BcnMjSourceBlock MJ_SOURCE = register("mj_source", BcnMjSourceBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_RED));
	public static final MjCableBlock CABLE_TIN = register("cable_tin",
			properties -> new MjCableBlock(MjPressure.ULV, 0.01, properties), cableProps(MapColor.CLAY));
	public static final MjCableBlock CABLE_COPPER = register("cable_copper",
			properties -> new MjCableBlock(MjPressure.LV, 0.08, properties), cableProps(MapColor.COLOR_ORANGE));
	public static final MjCableBlock CABLE_GOLD = register("cable_gold",
			properties -> new MjCableBlock(MjPressure.MV, 0.2, properties), cableProps(MapColor.GOLD));
	public static final MjCableBlock CABLE_GOLD_2X = register("cable_gold_2x",
			properties -> new MjCableBlock(MjPressure.MV, 0.18, properties), insulatedCableProps(MapColor.GOLD));
	public static final MjCableBlock CABLE_GOLD_4X = register("cable_gold_4x",
			properties -> new MjCableBlock(MjPressure.MV, 0.16, properties), insulatedCableProps(MapColor.GOLD));
	public static final MjCableBlock CABLE_GLASS_FIBER = register("cable_glass_fiber",
			properties -> new MjCableBlock(MjPressure.HV, 0.01, properties), cableProps(MapColor.DIAMOND));
	public static final MjCableBlock CABLE_HV = register("cable_hv",
			properties -> new MjCableBlock(MjPressure.EV, 0.4, properties), cableProps(MapColor.COLOR_BLACK));
	public static final MjCableBlock CABLE_HV_2X = register("cable_hv_2x",
			properties -> new MjCableBlock(MjPressure.EV, 0.36, properties), insulatedCableProps(MapColor.COLOR_BLACK));
	public static final MjCableBlock CABLE_HV_4X = register("cable_hv_4x",
			properties -> new MjCableBlock(MjPressure.EV, 0.32, properties), insulatedCableProps(MapColor.COLOR_BLACK));
	public static final MjCableDetectorBlock CABLE_DETECTOR = register("cable_detector",
			MjCableDetectorBlock::new, cableProps(MapColor.COLOR_RED));
	public static final MjCableSplitterBlock CABLE_SPLITTER = register("cable_splitter",
			MjCableSplitterBlock::new, cableProps(MapColor.COLOR_PURPLE));
	public static final Ic2GeneratorBlock GENERATOR_GEOTHERMAL = register("generator_geothermal",
			p -> new Ic2GeneratorBlock(Ic2GeneratorBlock.Gen.GEOTHERMAL, p), machineProps());
	public static final Ic2GeneratorBlock GENERATOR_WATER = register("generator_water",
			p -> new Ic2GeneratorBlock(Ic2GeneratorBlock.Gen.WATER, p), machineProps());
	public static final Ic2GeneratorBlock GENERATOR_WIND = register("generator_wind",
			p -> new Ic2GeneratorBlock(Ic2GeneratorBlock.Gen.WIND, p), machineProps());
	public static final Ic2GeneratorBlock GENERATOR_SOLAR = register("generator_solar",
			p -> new Ic2GeneratorBlock(Ic2GeneratorBlock.Gen.SOLAR, p), machineProps());
	public static final Ic2GeneratorBlock REACTOR = register("reactor",
			p -> new Ic2GeneratorBlock(Ic2GeneratorBlock.Gen.REACTOR, p), machineProps());
	public static final MjGeneratorBlock MJ_GENERATOR = register("mj_generator", MjGeneratorBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_RED)
					.requiresCorrectToolForDrops());
	public static final MjConsumerBlock MJ_CONSUMER = register("mj_consumer", MjConsumerBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_BLUE)
					.requiresCorrectToolForDrops());
	public static final MjTransformerBlock TRANSFORMER_LV = register("transformer_lv",
			properties -> new MjTransformerBlock(MjPressure.MV, MjPressure.LV, properties),
			machineProps());
	public static final MjTransformerBlock TRANSFORMER_MV = register("transformer_mv",
			properties -> new MjTransformerBlock(MjPressure.HV, MjPressure.MV, properties),
			machineProps());
	public static final MjTransformerBlock TRANSFORMER_HV = register("transformer_hv",
			properties -> new MjTransformerBlock(MjPressure.EV, MjPressure.HV, properties),
			machineProps());
	public static final MjBatteryBlock BATTERY_BOX = register("battery_box",
			properties -> new MjBatteryBlock(MjPressure.LV, 16000, properties), machineProps());
	public static final MjBatteryBlock BATBOX = register("batbox",
			properties -> new MjBatteryBlock(MjPressure.LV, 16000, properties), machineProps());
	public static final MjBatteryBlock MFE = register("mfe",
			properties -> new MjBatteryBlock(MjPressure.MV, 1600000, properties), machineProps());
	public static final MjBatteryBlock MFSU = register("mfsu",
			properties -> new MjBatteryBlock(MjPressure.EV, 4000000, properties), machineProps());
	public static final Block FUEL = register("fuel", properties -> new OilLiquidBlock(BcnFluids.FUEL, properties),
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.GOLD)
					.replaceable()
					.noCollision()
					.strength(100.0F)
					.pushReaction(net.minecraft.world.level.material.PushReaction.POPPED)
					.liquid()
					.noLootTable());
	public static final Block OIL_SPRING = register("oil_spring", OilSpringBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)
					.mapColor(MapColor.COLOR_BLACK)
					.randomTicks()
					.requiresCorrectToolForDrops());
	public static final BcnCannonBlock CANNON = register("cannon", BcnCannonBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.METAL)
					.requiresCorrectToolForDrops());
	public static final BcnTrebuchetBlock TREBUCHET = register("trebuchet", BcnTrebuchetBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).mapColor(MapColor.WOOD)
					.requiresCorrectToolForDrops());
	public static final BcnWireBlock REDWIRE = register("redwire", BcnWireBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_RED)
					.noCollision()
					.noOcclusion()
					.strength(0.1F)
					.pushReaction(net.minecraft.world.level.material.PushReaction.POPPED));
	public static final LogisticsPipeBlock PIPE_BASIC_LOGISTICS = register(
			"pipe_basic_logistics",
			properties -> new LogisticsPipeBlock(LpKind.BASIC, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LogisticsPipeBlock PIPE_PROVIDER = register(
			"pipe_provider",
			properties -> new LogisticsPipeBlock(LpKind.PROVIDER, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LogisticsPipeBlock PIPE_REQUEST = register(
			"pipe_request",
			properties -> new LogisticsPipeBlock(LpKind.REQUEST, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LogisticsPipeBlock PIPE_SUPPLIER = register(
			"pipe_supplier",
			properties -> new LogisticsPipeBlock(LpKind.SUPPLIER, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LogisticsPipeBlock PIPE_CRAFTING = register(
			"pipe_crafting",
			properties -> new LogisticsPipeBlock(LpKind.CRAFTING, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LogisticsPipeBlock PIPE_CHASSIS_MK1 = register(
			"pipe_chassis_mk1",
			properties -> new LogisticsPipeBlock(LpKind.CHASSIS1, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LogisticsPipeBlock PIPE_CHASSIS_MK2 = register(
			"pipe_chassis_mk2",
			properties -> new LogisticsPipeBlock(LpKind.CHASSIS2, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LogisticsPipeBlock PIPE_CHASSIS_MK3 = register(
			"pipe_chassis_mk3",
			properties -> new LogisticsPipeBlock(LpKind.CHASSIS3, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LogisticsPipeBlock PIPE_CHASSIS_MK4 = register(
			"pipe_chassis_mk4",
			properties -> new LogisticsPipeBlock(LpKind.CHASSIS4, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LogisticsPipeBlock PIPE_CHASSIS_MK5 = register(
			"pipe_chassis_mk5",
			properties -> new LogisticsPipeBlock(LpKind.CHASSIS5, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final LiquidSupplierPipeBlock PIPE_LIQUID_SUPPLIER = register(
			"pipe_liquid_supplier", LiquidSupplierPipeBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
	public static final SolderingStationBlock SOLDERING_STATION = register(
			"soldering_station", SolderingStationBlock::new, machineProps());
	public static final BcnGateBlock GATE_NOT = registerGate("gate_not", BcnGateBlock.GateType.NOT);
	public static final BcnGateBlock GATE_BUFFER = registerGate("gate_buffer", BcnGateBlock.GateType.BUFFER);
	public static final BcnGateBlock GATE_OR = registerGate("gate_or", BcnGateBlock.GateType.OR);
	public static final BcnGateBlock GATE_NOR = registerGate("gate_nor", BcnGateBlock.GateType.NOR);
	public static final BcnGateBlock GATE_AND = registerGate("gate_and", BcnGateBlock.GateType.AND);
	public static final BcnGateBlock GATE_NAND = registerGate("gate_nand", BcnGateBlock.GateType.NAND);
	public static final BcnGateBlock GATE_XOR = registerGate("gate_xor", BcnGateBlock.GateType.XOR);
	public static final BcnGateBlock GATE_XNOR = registerGate("gate_xnor", BcnGateBlock.GateType.XNOR);
	public static final BcnGateBlock GATE_RS_LATCH = registerGate("gate_rs_latch", BcnGateBlock.GateType.RS_LATCH);
	public static final BcnGateBlock GATE_T_LATCH = registerGate("gate_t_latch", BcnGateBlock.GateType.T_LATCH);

	// ---- Project:Red 系 ----

	public static final Map<DyeColor, BcnInsulatedWireBlock> INSULATED_WIRE = registerInsulatedWires();
	public static final BcnBundledCableBlock BUNDLED_CABLE = register("bundled_cable",
			BcnBundledCableBlock::new, wireProps().strength(0.3F));
	public static final BcnIcGateBlock IC_GATE = register("ic_gate", BcnIcGateBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.STONE)
					.noOcclusion()
					.strength(0.5F)
					.pushReaction(net.minecraft.world.level.material.PushReaction.POPPED));
	public static final BcnFrameBlock FRAME = register("frame", BcnFrameBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.WOOD)
					.noOcclusion()
					.strength(0.5F));
	public static final BcnFrameMotorBlock FRAME_MOTOR = register("frame_motor",
			BcnFrameMotorBlock::new, machineProps());
	public static final BcnMicroblockBlock MICROBLOCK = register("microblock", BcnMicroblockBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.STONE)
					.noOcclusion()
					.strength(1.0F));
	public static final Block BOULE = register("boule", Block::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK).mapColor(MapColor.COLOR_LIGHT_GRAY)
					.requiresCorrectToolForDrops());
	public static final MjBatteryBlock BT_BATTERY_BOX = register("bt_battery_box",
			properties -> new MjBatteryBlock(MjPressure.LV, 24000, properties), machineProps());
	public static final Ic2GeneratorBlock SOLAR_PANEL = register("solar_panel",
			p -> new Ic2GeneratorBlock(Ic2GeneratorBlock.Gen.SOLAR, p), machineProps());
	public static final CropStickBlock CROP_STICK = register("crop_stick", CropStickBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.WOOD)
					.noCollision()
					.noOcclusion()
					.strength(0.1F)
					.sound(net.minecraft.world.level.block.SoundType.WOOD)
					.pushReaction(net.minecraft.world.level.material.PushReaction.POPPED));
	public static final BoozeBarrelBlock BOOZE_BARREL = register("booze_barrel", BoozeBarrelBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL));
	public static final com.civilizationfrontier.bcn.builder.LandmarkBlock LANDMARK = register("landmark",
			p -> new com.civilizationfrontier.bcn.builder.LandmarkBlock(p, false),
			BlockBehaviour.Properties.of().noOcclusion().strength(0.3F)
					.lightLevel(s -> s.getValue(com.civilizationfrontier.bcn.builder.LandmarkBlock.ACTIVE) ? 12 : 0));
	public static final com.civilizationfrontier.bcn.builder.LandmarkBlock PATH_MARK = register("path_mark",
			p -> new com.civilizationfrontier.bcn.builder.LandmarkBlock(p, true),
			BlockBehaviour.Properties.of().noOcclusion().strength(0.3F)
					.lightLevel(s -> s.getValue(com.civilizationfrontier.bcn.builder.LandmarkBlock.ACTIVE) ? 12 : 0));
	public static final com.civilizationfrontier.bcn.builder.FillerBlock FILLER = register("filler",
			com.civilizationfrontier.bcn.builder.FillerBlock::new, machineProps());
	public static final com.civilizationfrontier.bcn.builder.BuilderBlock BUILDER = register("builder",
			com.civilizationfrontier.bcn.builder.BuilderBlock::new, machineProps());
	public static final com.civilizationfrontier.bcn.builder.ArchitectTableBlock ARCHITECT_TABLE = register("architect_table",
			com.civilizationfrontier.bcn.builder.ArchitectTableBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE));

	private static Map<DyeColor, BcnInsulatedWireBlock> registerInsulatedWires() {
		Map<DyeColor, BcnInsulatedWireBlock> map = new EnumMap<>(DyeColor.class);
		for (DyeColor color : DyeColor.values()) {
			map.put(color, register("insulated_wire_" + color.getName(),
					p -> new BcnInsulatedWireBlock(p, color), wireProps()));
		}
		return Map.copyOf(map);
	}

	private static BlockBehaviour.Properties wireProps() {
		return BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.noCollision()
				.noOcclusion()
				.strength(0.1F)
				.pushReaction(net.minecraft.world.level.material.PushReaction.POPPED);
	}

	private static BcnGateBlock registerGate(String name, BcnGateBlock.GateType type) {
		return register(name, properties -> new BcnGateBlock(properties, type),
				BlockBehaviour.Properties.of()
						.mapColor(MapColor.STONE)
						.noOcclusion()
						.strength(0.5F)
						.pushReaction(net.minecraft.world.level.material.PushReaction.POPPED));
	}

	private static BlockBehaviour.Properties cableProps(MapColor color) {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).mapColor(color).noOcclusion()
				.strength(0.5F);
	}

	private static BlockBehaviour.Properties insulatedCableProps(MapColor color) {
		return cableProps(color).strength(2.0F);
	}

	private static BlockBehaviour.Properties machineProps() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops();
	}

	private static Block registerOre(String name, Block copyFrom) {
		return register(name, Block::new, BlockBehaviour.Properties.ofFullCopy(copyFrom).requiresCorrectToolForDrops());
	}

	private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory,
			BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Bcn.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
	}
}
