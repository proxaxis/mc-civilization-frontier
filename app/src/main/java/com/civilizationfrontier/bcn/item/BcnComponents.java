package com.civilizationfrontier.bcn.item;

import com.civilizationfrontier.bcn.Bcn;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class BcnComponents {

	/** アイテム内に保持する MJ 量. バッテリー系アイテムが使用する. */
	public static final DataComponentType<Integer> MJ_STORED = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, Bcn.id("mj_stored"),
			DataComponentType.<Integer>builder()
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT)
					.build());

	/** 種袋が保持する作物種 ID ({@link com.civilizationfrontier.bcn.crop.BcnCropType}). */
	public static final DataComponentType<String> CROP_ID = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, Bcn.id("crop_id"),
			DataComponentType.<String>builder()
					.persistent(Codec.STRING)
					.networkSynchronized(ByteBufCodecs.STRING_UTF8)
					.build());

	/** 種袋が保持する GGR ステータス (1-31). */
	public static final DataComponentType<Integer> CROP_GROWTH = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, Bcn.id("crop_growth"),
			DataComponentType.<Integer>builder()
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT)
					.build());
	public static final DataComponentType<Integer> CROP_GAIN = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, Bcn.id("crop_gain"),
			DataComponentType.<Integer>builder()
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT)
					.build());
	public static final DataComponentType<Integer> CROP_RESISTANCE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, Bcn.id("crop_resistance"),
			DataComponentType.<Integer>builder()
					.persistent(Codec.INT)
					.networkSynchronized(ByteBufCodecs.VAR_INT)
					.build());

	/** 種袋が作物分析機で鑑定済みか. */
	public static final DataComponentType<Boolean> CROP_SCANNED = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, Bcn.id("crop_scanned"),
			DataComponentType.<Boolean>builder()
					.persistent(Codec.BOOL)
					.networkSynchronized(ByteBufCodecs.BOOL)
					.build());

	private BcnComponents() {
	}

	public static void init() {
	}
}
