package com.civilizationfrontier.bcn.block;

import java.util.function.Function;

import com.civilizationfrontier.bcn.Bcn;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.EngineKind;
import com.civilizationfrontier.bcn.energy.block.MjBatteryBlock;
import com.civilizationfrontier.bcn.energy.block.MjCableBlock;
import com.civilizationfrontier.bcn.energy.block.MjConsumerBlock;
import com.civilizationfrontier.bcn.energy.block.MjGeneratorBlock;
import com.civilizationfrontier.bcn.energy.block.MjRefineryBlock;
import com.civilizationfrontier.bcn.energy.block.MjTransformerBlock;
import com.civilizationfrontier.bcn.fluid.BcnFluids;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneOreBlock;
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
	public static final BcnEngineBlock ENGINE_COMBUSTION = register("engine_combustion",
			properties -> new BcnEngineBlock(EngineKind.COMBUSTION, properties),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.METAL)
					.requiresCorrectToolForDrops());
	public static final MjRefineryBlock REFINERY = register("refinery", MjRefineryBlock::new, machineProps());
	public static final BcnPipeBlock ITEM_PIPE = register("item_pipe",
			properties -> new BcnPipeBlock(properties, false),
			BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).noOcclusion());
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
			properties -> new MjCableBlock(MjPressure.MV, 0.16, properties), cableProps(MapColor.GOLD));
	public static final MjCableBlock CABLE_GLASS_FIBER = register("cable_glass_fiber",
			properties -> new MjCableBlock(MjPressure.HV, 0.01, properties), cableProps(MapColor.DIAMOND));
	public static final MjCableBlock CABLE_HV = register("cable_hv",
			properties -> new MjCableBlock(MjPressure.EV, 0.32, properties), cableProps(MapColor.COLOR_BLACK));
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
	public static final MjBatteryBlock MFE = register("mfe",
			properties -> new MjBatteryBlock(MjPressure.MV, 1600000, properties), machineProps());
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

	private static BcnGateBlock registerGate(String name, BcnGateBlock.GateType type) {
		return register(name, properties -> new BcnGateBlock(properties, type),
				BlockBehaviour.Properties.of()
						.mapColor(MapColor.STONE)
						.noOcclusion()
						.strength(0.5F)
						.pushReaction(net.minecraft.world.level.material.PushReaction.POPPED));
	}

	private static BlockBehaviour.Properties cableProps(MapColor color) {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).mapColor(color).noOcclusion();
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
