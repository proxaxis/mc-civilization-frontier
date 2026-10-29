package com.civilizationfrontier.bcn.entity;

import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 大砲弾. 高速・直進・小爆発. */
public class CannonballEntity extends BcnShellEntity {

	public CannonballEntity(EntityType<? extends CannonballEntity> type, Level level) {
		super(type, level);
	}

	public CannonballEntity(Level level, Vec3 pos) {
		super(BcnEntities.CANNONBALL, level, pos);
	}

	@Override
	protected Item getDefaultItem() {
		return BcnItems.CANNONBALL;
	}

	@Override
	protected float explosionPower() {
		return 2.0F;
	}

	@Override
	protected long impactEfp() {
		return 6L;
	}

	@Override
	protected int maxAge() {
		return 200;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.03;
	}
}
