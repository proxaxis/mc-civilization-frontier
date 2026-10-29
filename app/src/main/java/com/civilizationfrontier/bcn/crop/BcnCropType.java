package com.civilizationfrontier.bcn.crop;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 育種可能な作物種の定義. IC2 crops の近似.
 *
 * - tier: 品種階級 (交差育種の突然変異は親の最大 tier+1 まで出現)
 * - maxStage: 成熟までの成長段階数
 * - seed: 支柱への植え付けに使う種アイテム (null は種から直接植えられない)
 * - drops: 収穫物 (個数は gain ステータスでスケール)
 */
public enum BcnCropType {

	WEED("weed", 0, 5, null, List.of()),
	WHEAT("wheat", 1, 7, () -> Items.WHEAT_SEEDS,
			List.of(drop(() -> Items.WHEAT, 1, 2), drop(() -> Items.WHEAT_SEEDS, 0, 2))),
	PUMPKIN("pumpkin", 1, 7, () -> Items.PUMPKIN_SEEDS,
			List.of(drop(() -> Items.PUMPKIN, 1, 1))),
	CARROT("carrot", 2, 7, () -> Items.CARROT,
			List.of(drop(() -> Items.CARROT, 1, 3))),
	POTATO("potato", 2, 7, () -> Items.POTATO,
			List.of(drop(() -> Items.POTATO, 1, 3))),
	MELON("melon", 2, 7, () -> Items.MELON_SEEDS,
			List.of(drop(() -> Items.MELON, 1, 1))),
	SUGARCANE("sugarcane", 2, 7, () -> Items.SUGAR_CANE,
			List.of(drop(() -> Items.SUGAR_CANE, 1, 2))),
	BEETROOT("beetroot", 3, 7, () -> Items.BEETROOT_SEEDS,
			List.of(drop(() -> Items.BEETROOT, 1, 2), drop(() -> Items.BEETROOT_SEEDS, 0, 2))),
	COCOA("cocoa", 3, 5, () -> Items.COCOA_BEANS,
			List.of(drop(() -> Items.COCOA_BEANS, 1, 2))),
	NETHER_WART("nether_wart", 5, 5, () -> Items.NETHER_WART,
			List.of(drop(() -> Items.NETHER_WART, 1, 2))),
	COFFEE("coffee", 5, 7, () -> BcnItems.COFFEE_BEANS,
			List.of(drop(() -> BcnItems.COFFEE_BEANS, 1, 2))),
	HOPS("hops", 6, 7, () -> BcnItems.HOPS,
			List.of(drop(() -> BcnItems.HOPS, 1, 2)));

	private final String id;
	private final int tier;
	private final int maxStage;
	private final Supplier<Item> seed;
	private final List<CropDrop> drops;

	BcnCropType(String id, int tier, int maxStage, Supplier<Item> seed, List<CropDrop> drops) {
		this.id = id;
		this.tier = tier;
		this.maxStage = maxStage;
		this.seed = seed;
		this.drops = drops;
	}

	public String id() {
		return id;
	}

	public int tier() {
		return tier;
	}

	public int maxStage() {
		return maxStage;
	}

	/** 種袋に格納される品種 ID から種を引く. */
	public static BcnCropType fromId(String id) {
		for (BcnCropType type : values()) {
			if (type.id.equals(id)) return type;
		}
		return null;
	}

	/** 手持ちアイテムがそのまま植えられる種なら対応する作物種を返す. */
	public static BcnCropType forSeed(Item item) {
		for (BcnCropType type : values()) {
			if (type.seed != null && type.seed.get() == item) return type;
		}
		return null;
	}

	/** 収穫ドロップを生成する. gain ステータスが高いほど個数が増える. */
	public List<ItemStack> rollDrops(int gain, RandomSource random) {
		List<ItemStack> out = new ArrayList<>();
		for (CropDrop drop : drops) {
			int count = drop.min() + random.nextInt(drop.max() - drop.min() + 1) + gain / 10;
			if (count > 0) out.add(new ItemStack(drop.item().get(), count));
		}
		return out;
	}

	/** 突然変異先候補: maxTier 以下の非雑草種. */
	public static BcnCropType mutate(int parentTierA, int parentTierB, RandomSource random) {
		int cap = Math.max(parentTierA, parentTierB) + 1;
		List<BcnCropType> candidates = new ArrayList<>();
		for (BcnCropType type : values()) {
			if (type != WEED && type.tier <= cap) candidates.add(type);
		}
		return candidates.get(random.nextInt(candidates.size()));
	}

	private static CropDrop drop(Supplier<Item> item, int min, int max) {
		return new CropDrop(item, min, max);
	}

	private record CropDrop(Supplier<Item> item, int min, int max) {
	}
}
