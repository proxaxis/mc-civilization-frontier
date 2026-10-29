package com.civilizationfrontier.bcn.entity;

import com.civilizationfrontier.bcn.Bcn;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class BcnEntities {

	/** サーバー全体で同時に存在できる砲弾の上限. 超過時は発射を拒否する. */
	public static final int MAX_LIVE_SHELLS = 48;

	public static final EntityType<CannonballEntity> CANNONBALL = register("cannonball",
			EntityType.Builder.<CannonballEntity>of(CannonballEntity::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(8)
					.updateInterval(10)
					.fireImmune()
					.noSave());

	public static final EntityType<SiegeShotEntity> SIEGE_SHOT = register("siege_shot",
			EntityType.Builder.<SiegeShotEntity>of(SiegeShotEntity::new, MobCategory.MISC)
					.sized(1.0F, 1.0F)
					.clientTrackingRange(8)
					.updateInterval(10)
					.fireImmune()
					.noSave());

	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Bcn.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	/** 発射許容判定. ワールド内の砲弾数が上限未満なら true. */
	public static boolean hasShellCapacity(ServerLevel level) {
		return level.getEntities(CANNONBALL, entity -> true).size()
				+ level.getEntities(SIEGE_SHOT, entity -> true).size() < MAX_LIVE_SHELLS;
	}

	public static void init() {
	}
}
