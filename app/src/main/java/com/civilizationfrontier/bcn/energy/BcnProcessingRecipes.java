package com.civilizationfrontier.bcn.energy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.civilizationfrontier.bcn.Bcn;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加工機レシピのデータ駆動ローダー.
 * `data/<ns>/processing/<machine>.json` を全 namespace から読み、機種ごとにレシピを保持する.
 * datapack で同名/別名ファイルを追加すればレシピ追加・上書きが可能.
 *
 * 形式:
 * { "recipes": [ { "in": [["item", count], ...], "out": ["item", count],
 *                  "ticks": 400, "mj_per_tick": 4.0 }, ... ] }
 */
public final class BcnProcessingRecipes implements SimpleSynchronousResourceReloadListener {

	public static final Identifier ID = Bcn.id("processing");

	public record Ingredient(Item item, int count) {
	}

	public record Recipe(List<Ingredient> inputs, Item output, int outCount, int ticks, double mjPerTick) {
	}

	private static final Map<String, List<Recipe>> RECIPES = new HashMap<>();
	private static final BcnProcessingRecipes INSTANCE = new BcnProcessingRecipes();

	private BcnProcessingRecipes() {
	}

	public static void init() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(INSTANCE);
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		RECIPES.clear();
		for (Map.Entry<Identifier, Resource> entry
				: manager.listResources("processing", id -> id.getPath().endsWith(".json")).entrySet()) {
			String file = entry.getKey().getPath();
			String machine = file.substring("processing/".length(), file.length() - ".json".length());
			try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
				JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
				List<Recipe> list = RECIPES.computeIfAbsent(machine, k -> new ArrayList<>());
				for (JsonElement e : root.getAsJsonArray("recipes")) {
					list.add(parse(e.getAsJsonObject()));
				}
			} catch (Exception ex) {
				System.err.println("[bcn] failed to load processing recipe " + entry.getKey() + ": " + ex);
			}
		}
	}

	private static Recipe parse(JsonObject obj) {
		List<Ingredient> inputs = new ArrayList<>();
		for (JsonElement e : obj.getAsJsonArray("in")) {
			JsonArray pair = e.getAsJsonArray();
			inputs.add(new Ingredient(item(pair.get(0).getAsString()),
					pair.size() > 1 ? pair.get(1).getAsInt() : 1));
		}
		JsonArray out = obj.getAsJsonArray("out");
		return new Recipe(inputs, item(out.get(0).getAsString()),
				out.size() > 1 ? out.get(1).getAsInt() : 1,
				obj.has("ticks") ? obj.get("ticks").getAsInt() : 400,
				obj.has("mj_per_tick") ? obj.get("mj_per_tick").getAsDouble() : 4.0);
	}

	private static Item item(String name) {
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(name));
		if (item == null || item == net.minecraft.world.item.Items.AIR) {
			throw new IllegalArgumentException("unknown item " + name);
		}
		return item;
	}

	/** 機種 machine に対し、スロット内容で最初に成立するレシピを返す. */
	public static Recipe find(String machine, NonNullList<ItemStack> inputs) {
		List<Recipe> list = RECIPES.get(machine);
		if (list == null) {
			return null;
		}
		for (Recipe recipe : list) {
			if (matches(recipe, inputs)) {
				return recipe;
			}
		}
		return null;
	}

	private static boolean matches(Recipe recipe, NonNullList<ItemStack> slots) {
		for (Ingredient in : recipe.inputs()) {
			int have = 0;
			for (int i = 0; i < 2 && i < slots.size(); i++) {
				if (slots.get(i).is(in.item())) {
					have += slots.get(i).getCount();
				}
			}
			if (have < in.count()) {
				return false;
			}
		}
		return true;
	}

	/** recipe の入力を inputs スロットから消費する. */
	public static void consume(Recipe recipe, NonNullList<ItemStack> slots) {
		for (Ingredient in : recipe.inputs()) {
			int need = in.count();
			for (int i = 0; i < 2 && i < slots.size() && need > 0; i++) {
				ItemStack stack = slots.get(i);
				if (stack.is(in.item())) {
					int take = Math.min(need, stack.getCount());
					stack.shrink(take);
					need -= take;
				}
			}
		}
	}
}
