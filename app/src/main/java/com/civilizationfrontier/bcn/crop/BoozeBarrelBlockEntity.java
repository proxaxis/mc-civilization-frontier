package com.civilizationfrontier.bcn.crop;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 酒樽の BlockEntity. 仕込んだ液体を経過時間で発酵させる (IC2 Booze Barrel 近似).
 *
 * - レシピ: 水 + 砂糖 3 でラム, 水 + 小麦 2 + ホップ 1 で麦酒系.
 * - 発酵時間 (ferment_ticks) で産物が変化:
 *   ラム <24000=未発酵, >=24000=熟成. 麦酒 <6000=若い酒, <18000=ビール, <36000=エール, それ以上=失敗作 (暗黒物質X).
 * - 各仕込みは 4 杯分 (servings). マグカップで汲み出す.
 */
public class BoozeBarrelBlockEntity extends BlockEntity {

	public static final String BREW_RUM = "rum";
	public static final String BREW_BEER = "beer";

	public static final int RUM_AGED_TICKS = 24000;
	public static final int BEER_YOUNG_TICKS = 6000;
	public static final int BEER_MATURE_TICKS = 18000;
	public static final int BEER_OVER_TICKS = 36000;
	public static final int SERVINGS_PER_BREW = 4;

	private String brew = "";
	private int water;
	private int sugar;
	private int wheat;
	private int hops;
	private int fermentTicks;
	private int servings;

	public BoozeBarrelBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.BOOZE_BARREL, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BoozeBarrelBlockEntity be) {
		if (level.isClientSide()) return;
		if (!be.brew.isEmpty() && be.servings > 0) {
			be.fermentTicks++;
			be.setChanged();
		}
	}

	public boolean addWater() {
		if (!brew.isEmpty() || water > 0) return false;
		water = 1;
		resolveBrew();
		setChanged();
		return true;
	}

	public boolean addSugar() {
		if (!brew.isEmpty()) return false;
		sugar++;
		resolveBrew();
		setChanged();
		return true;
	}

	public boolean addWheat() {
		if (!brew.isEmpty()) return false;
		wheat++;
		resolveBrew();
		setChanged();
		return true;
	}

	public boolean addHops() {
		if (!brew.isEmpty()) return false;
		hops++;
		resolveBrew();
		setChanged();
		return true;
	}

	/** 材料が揃った時点で仕込み確定し発酵を開始する. */
	private void resolveBrew() {
		if (!brew.isEmpty() || water <= 0) return;
		if (sugar >= 3) {
			brew = BREW_RUM;
			servings = SERVINGS_PER_BREW;
			fermentTicks = 0;
		} else if (wheat >= 2 && hops >= 1) {
			brew = BREW_BEER;
			servings = SERVINGS_PER_BREW;
			fermentTicks = 0;
		}
	}

	/** マグカップでの汲み出し. 発酵段階に応じた飲料アイテム ID を返す. */
	public String serveDrink() {
		if (servings <= 0 || brew.isEmpty()) return "";
		String result;
		if (BREW_RUM.equals(brew)) {
			result = fermentTicks >= RUM_AGED_TICKS ? "mug_rum_aged" : "mug_rum";
		} else {
			if (fermentTicks < BEER_YOUNG_TICKS) result = "mug_youngster";
			else if (fermentTicks < BEER_MATURE_TICKS) result = "mug_beer";
			else if (fermentTicks < BEER_OVER_TICKS) result = "mug_ale";
			else result = "mug_black_stuff";
		}
		if (--servings <= 0) reset();
		setChanged();
		return result;
	}

	public boolean brewing() {
		return !brew.isEmpty();
	}

	public String brew() {
		return brew;
	}

	public int fermentTicks() {
		return fermentTicks;
	}

	public int servings() {
		return servings;
	}

	public int waterStored() {
		return water;
	}

	public int sugar() {
		return sugar;
	}

	public int wheat() {
		return wheat;
	}

	public int hops() {
		return hops;
	}

	private void reset() {
		brew = "";
		water = 0;
		sugar = 0;
		wheat = 0;
		hops = 0;
		fermentTicks = 0;
		servings = 0;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("brew", brew);
		output.putInt("water", water);
		output.putInt("sugar", sugar);
		output.putInt("wheat", wheat);
		output.putInt("hops", hops);
		output.putInt("ferment_ticks", fermentTicks);
		output.putInt("servings", servings);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		brew = input.getStringOr("brew", "");
		water = input.getIntOr("water", 0);
		sugar = input.getIntOr("sugar", 0);
		wheat = input.getIntOr("wheat", 0);
		hops = input.getIntOr("hops", 0);
		fermentTicks = input.getIntOr("ferment_ticks", 0);
		servings = input.getIntOr("servings", 0);
	}
}
