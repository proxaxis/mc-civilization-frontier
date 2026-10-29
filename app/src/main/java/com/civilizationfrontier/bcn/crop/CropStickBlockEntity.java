package com.civilizationfrontier.bcn.crop;

import java.util.ArrayList;
import java.util.List;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.item.BcnSeedBagItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 園芸用支柱の BlockEntity. IC2 crops の近似実装.
 *
 * - 管理する状態: 作物種・成長段階・GGR ステータス (growth/gain/resistance)
 * - 5 パラメータ: 光量 (sky light) / 通気 (周囲の空気量) / 栄養 (肥料ストック+耕地湿潤)
 *   / 保水 (給水ストック+周囲水源) / 除草 (除草剤ストック). 肥料系アイテムで直接増減し tick 毎に減衰.
 * - 二重支柱 (cross=true) は交差育種ベース. 隣接 4 方向の成熟作物 2 株を親に子を育てる.
 * - 雑草: 栄養過剰・除草剤不足で発生し、隣の作物を雑草化させることがある.
 */
public class CropStickBlockEntity extends BlockEntity {

	public static final int TICK_INTERVAL = 20;
	public static final int POINTS_PER_STAGE = 240;
	public static final int MAX_STORAGE = 100;

	private static final double WEED_SPAWN_CHANCE = 0.02;
	private static final double WEED_SPREAD_CHANCE = 0.05;
	private static final double BREED_CHANCE = 0.15;

	private String crop = "";
	private int stage;
	private int growth;
	private int gain;
	private int resistance;
	private int growProgress;
	private int nutrients;
	private int water;
	private int weedEx;
	private int cooldown;

	public CropStickBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.CROP_STICK, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CropStickBlockEntity be) {
		if (level.isClientSide() || !(level instanceof ServerLevel server)) return;
		if (++be.cooldown < TICK_INTERVAL) return;
		be.cooldown = 0;

		be.nutrients = Math.max(0, be.nutrients - 1);
		be.water = Math.max(0, be.water - 1);
		be.weedEx = Math.max(0, be.weedEx - 1);

		RandomSource random = server.getRandom();
		boolean cross = state.getValue(CropStickBlock.CROSS);

		if (be.crop.isEmpty()) {
			if (cross) {
				be.tryBreed(server, pos, random);
			} else if (be.nutrients >= 40 && be.weedEx <= 0 && random.nextDouble() < WEED_SPAWN_CHANCE) {
				be.setCrop(BcnCropType.WEED, 1, 1, 1);
			}
			return;
		}

		BcnCropType type = BcnCropType.fromId(be.crop);
		if (type == null) {
			be.clearCrop();
			return;
		}

		// 除草剤が効いている雑草は枯れる
		if (type == BcnCropType.WEED && be.weedEx > 0) {
			be.clearCrop();
			return;
		}

		// 雑草の繁殖: 耐性の低い隣の作物を雑草化する
		if (type == BcnCropType.WEED && be.stage >= 2 && random.nextDouble() < WEED_SPREAD_CHANCE) {
			be.spreadWeed(server, pos, random);
		}

		// 成長
		int rate = type == BcnCropType.WEED ? 12 : be.growth + be.envScore(server, pos);
		if (be.weedEx > 0 && type != BcnCropType.WEED) rate = Math.max(1, rate - 2);
		be.growProgress += rate;
		boolean grew = false;
		while (be.growProgress >= POINTS_PER_STAGE && be.stage < type.maxStage()) {
			be.growProgress -= POINTS_PER_STAGE;
			be.stage++;
			grew = true;
		}
		if (be.growProgress >= POINTS_PER_STAGE) be.growProgress = POINTS_PER_STAGE;

		if (grew) be.updateVisual(server, pos, state);
		be.setChanged();
	}

	/** 隣接 4 方向の成熟した通常支柱を親に、交差育種の子をこの支柱へ植える. */
	private void tryBreed(ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextDouble() >= BREED_CHANCE) return;
		List<CropStickBlockEntity> parents = new ArrayList<>();
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			if (level.getBlockEntity(pos.relative(dir)) instanceof CropStickBlockEntity other
					&& !other.isCross() && other.hasCrop() && other.isMature()) {
				parents.add(other);
			}
		}
		if (parents.size() < 2) return;
		CropStickBlockEntity pa = parents.get(random.nextInt(parents.size()));
		CropStickBlockEntity pb = parents.get(random.nextInt(parents.size()));
		if (pa == pb) return;

		BcnCropType ta = BcnCropType.fromId(pa.crop);
		BcnCropType tb = BcnCropType.fromId(pb.crop);
		BcnCropType child;
		double roll = random.nextDouble();
		if (roll < 0.6) {
			child = random.nextBoolean() ? ta : tb;
		} else if (roll < 0.85) {
			child = BcnCropType.mutate(ta.tier(), tb.tier(), random);
		} else {
			child = BcnCropType.WEED;
		}
		setCrop(child,
				crossStat(pa.growth, pb.growth, random),
				crossStat(pa.gain, pb.gain, random),
				crossStat(pa.resistance, pb.resistance, random));
	}

	private static int crossStat(int a, int b, RandomSource random) {
		return Math.clamp((a + b + 1) / 2 + random.nextInt(-1, 2), 1, 31);
	}

	private void spreadWeed(ServerLevel level, BlockPos pos, RandomSource random) {
		List<Direction> dirs = Direction.Plane.HORIZONTAL.stream().toList();
		Direction dir = dirs.get(random.nextInt(dirs.size()));
		if (level.getBlockEntity(pos.relative(dir)) instanceof CropStickBlockEntity other
				&& other.hasCrop() && !BcnCropType.WEED.id().equals(other.crop)
				&& other.resistance <= 20 && other.weedEx <= 0) {
			other.setCrop(BcnCropType.WEED, 1, 1, 1);
		}
	}

	public void plant(BcnCropType type, int growthStat, int gainStat, int resistanceStat) {
		setCrop(type, growthStat, gainStat, resistanceStat);
		setChanged();
	}

	private void setCrop(BcnCropType type, int g, int ga, int r) {
		crop = type.id();
		stage = 0;
		growth = g;
		gain = ga;
		resistance = r;
		growProgress = 0;
		setChanged();
		if (level != null) updateVisual(level, worldPosition, getBlockState());
	}

	public void clearCrop() {
		crop = "";
		stage = 0;
		growProgress = 0;
		setChanged();
		if (level != null) updateVisual(level, worldPosition, getBlockState());
	}

	private void updateVisual(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide()) return;
		int visual = visualStage();
		if (state.getValue(CropStickBlock.GROWTH) != visual) {
			level.setBlock(pos, state.setValue(CropStickBlock.GROWTH, visual), 3);
		}
	}

	private int visualStage() {
		BcnCropType type = BcnCropType.fromId(crop);
		if (type == null || crop.isEmpty()) return 0;
		return Math.min(3, (stage + 1) * 4 / (type.maxStage() + 1));
	}

	public boolean hasCrop() {
		return !crop.isEmpty();
	}

	public boolean isCross() {
		return getBlockState().getValue(CropStickBlock.CROSS);
	}

	public boolean isMature() {
		BcnCropType type = BcnCropType.fromId(crop);
		return type != null && stage >= type.maxStage();
	}

	public BcnCropType cropType() {
		return BcnCropType.fromId(crop);
	}

	public int stage() {
		return stage;
	}

	public int growthStat() {
		return growth;
	}

	public int gainStat() {
		return gain;
	}

	public int resistanceStat() {
		return resistance;
	}

	public int nutrients() {
		return nutrients;
	}

	public int waterStored() {
		return water;
	}

	public int weedExStored() {
		return weedEx;
	}

	public void addNutrients(int amount) {
		nutrients = Math.min(MAX_STORAGE, nutrients + amount);
		setChanged();
	}

	public void addWater(int amount) {
		water = Math.min(MAX_STORAGE, water + amount);
		setChanged();
	}

	public void addWeedEx(int amount) {
		weedEx = Math.min(MAX_STORAGE, weedEx + amount);
		setChanged();
	}

	/** 5 パラメータのうち環境由来の 4 つを計測し成長速度スコア (0-8) を返す. */
	private int envScore(ServerLevel level, BlockPos pos) {
		return Math.clamp((lightParam(level, pos) + airParam(level, pos)
				+ nutritionParam(level, pos) + hydrationParam(level, pos)) / 10, 0, 8);
	}

	/** 光量 0-20. */
	public int lightParam(Level level, BlockPos pos) {
		return level.getMaxLocalRawBrightness(pos) * 20 / 15;
	}

	/** 通気 0-20. 周囲 3x3x3 の空気ブロック割合. */
	public int airParam(Level level, BlockPos pos) {
		int air = 0;
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
			if (!p.equals(pos) && level.getBlockState(p).isAir()) air++;
		}
		return air * 20 / 26;
	}

	/** 栄養 0-20. 肥料ストック + 耕地湿潤度. */
	public int nutritionParam(Level level, BlockPos pos) {
		int score = nutrients / 5;
		BlockState below = level.getBlockState(pos.below());
		if (below.getBlock() instanceof FarmlandBlock) {
			score += below.getValue(FarmlandBlock.MOISTURE);
		}
		return Math.min(20, score);
	}

	/** 保水 0-20. 給水ストック + 耕地湿潤度 + 周囲の水源数. */
	public int hydrationParam(Level level, BlockPos pos) {
		int score = water / 5;
		BlockState below = level.getBlockState(pos.below());
		if (below.getBlock() instanceof FarmlandBlock) {
			score += below.getValue(FarmlandBlock.MOISTURE);
		}
		int sources = 0;
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, -1, 2))) {
			if (level.getFluidState(p).isSourceOfType(Fluids.WATER)) sources++;
		}
		return Math.min(20, score + sources);
	}

	/** 除草 0-20. 除草剤ストック. */
	public int herbicideParam() {
		return weedEx / 5;
	}

	/** 右クリック収穫. 成熟していれば収穫物を落とし成長段階を 2 つ戻す. 雑草は引き抜き. */
	public boolean harvest(ServerLevel level, BlockPos pos, RandomSource random) {
		BcnCropType type = BcnCropType.fromId(crop);
		if (type == null) return false;
		if (type == BcnCropType.WEED) {
			clearCrop();
			return true;
		}
		if (!isMature()) return false;
		for (var drop : type.rollDrops(gain, random)) {
			Block.popResource(level, pos, drop);
		}
		stage = Math.max(1, type.maxStage() - 3);
		growProgress = 0;
		updateVisual(level, pos, getBlockState());
		setChanged();
		return true;
	}

	/** 破壊時のドロップ: 育種中の種袋 (未鑑定) + 成熟時は収穫物. */
	public void dropContents(ServerLevel level, BlockPos pos) {
		BcnCropType type = BcnCropType.fromId(crop);
		if (type == null || type == BcnCropType.WEED) return;
		RandomSource random = level.getRandom();
		if (isMature()) {
			for (var drop : type.rollDrops(gain, random)) {
				Block.popResource(level, pos, drop);
			}
		}
		// 種袋ドロップ率: 未成熟ほど高い (成熟株は収穫物で代用)
		double bagChance = isMature() ? 0.25 : 0.4;
		if (random.nextDouble() < bagChance) {
			Block.popResource(level, pos, BcnSeedBagItem.of(type, growth, gain, resistance, false));
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		if (level instanceof ServerLevel server) dropContents(server, pos);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("crop", crop);
		output.putInt("stage", stage);
		output.putInt("growth", growth);
		output.putInt("gain", gain);
		output.putInt("resistance", resistance);
		output.putInt("grow_progress", growProgress);
		output.putInt("nutrients", nutrients);
		output.putInt("water", water);
		output.putInt("weedex", weedEx);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		crop = input.getStringOr("crop", "");
		stage = input.getIntOr("stage", 0);
		growth = input.getIntOr("growth", 0);
		gain = input.getIntOr("gain", 0);
		resistance = input.getIntOr("resistance", 0);
		growProgress = input.getIntOr("grow_progress", 0);
		nutrients = input.getIntOr("nutrients", 0);
		water = input.getIntOr("water", 0);
		weedEx = input.getIntOr("weedex", 0);
	}
}
