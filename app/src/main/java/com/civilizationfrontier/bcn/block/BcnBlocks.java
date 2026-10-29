package com.civilizationfrontier.bcn.block;

import java.util.function.Function;

import com.civilizationfrontier.bcn.Bcn;
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
	public static final Block OIL_SPRING = register("oil_spring", OilSpringBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)
					.mapColor(MapColor.COLOR_BLACK)
					.randomTicks()
					.requiresCorrectToolForDrops());

	private static Block registerOre(String name, Block copyFrom) {
		return register(name, Block::new, BlockBehaviour.Properties.ofFullCopy(copyFrom).requiresCorrectToolForDrops());
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory,
			BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Bcn.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	public static void init() {
	}
}
