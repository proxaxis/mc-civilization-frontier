package com.civilizationfrontier.bcn.worldgen;

import com.civilizationfrontier.bcn.Bcn;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class BcnWorldgen {
	private static final TagKey<Biome> IS_DESERT = conventionTag("is_desert");
	private static final TagKey<Biome> IS_OCEAN = conventionTag("is_ocean");

	public static void init() {
		addOverworldOres("copper_ore");
		addOverworldOres("tin_ore");
		addOverworldOres("silver_ore");
		addOverworldOres("uranium_ore");
		addOverworldOres("ruby_ore");
		addOverworldOres("sapphire_ore");
		addOverworldOres("peridot_ore");
		addOverworldOres("electrotine_ore");
		addOverworldOres("marble");
		addOverworldOres("oil_pocket_sparse");

		BiomeModifications.addFeature(BiomeSelectors.tag(IS_DESERT).or(BiomeSelectors.tag(IS_OCEAN)),
				GenerationStep.Decoration.UNDERGROUND_ORES, placedFeature("oil_pocket"));
		BiomeModifications.addFeature(BiomeSelectors.tag(IS_DESERT), GenerationStep.Decoration.LAKES,
				placedFeature("oil_field_desert"));
		BiomeModifications.addFeature(BiomeSelectors.tag(IS_DESERT), GenerationStep.Decoration.LAKES,
				placedFeature("oil_pond_desert"));
		BiomeModifications.addFeature(BiomeSelectors.tag(IS_OCEAN), GenerationStep.Decoration.LAKES,
				placedFeature("oil_field_ocean"));
	}

	private static void addOverworldOres(String name) {
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
				placedFeature(name));
	}

	private static ResourceKey<PlacedFeature> placedFeature(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, Bcn.id(name));
	}

	private static TagKey<Biome> conventionTag(String name) {
		return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", name));
	}
}
