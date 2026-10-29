package com.civilizationfrontier.bcn.entity;

import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 攻城兵器の巨石. 放物線・大爆発. */
public class SiegeShotEntity extends BcnShellEntity {

	public SiegeShotEntity(EntityType<? extends SiegeShotEntity> type, Level level) {
		super(type, level);
	}

	public SiegeShotEntity(Level level, Vec3 pos) {
		super(BcnEntities.SIEGE_SHOT, level, pos);
	}

	@Override
	protected Item getDefaultItem() {
		return BcnItems.SIEGE_SHOT;
	}

	@Override
	protected float explosionPower() {
		return 3.5F;
	}

	@Override
	protected long impactEfp() {
		return 12L;
	}

	@Override
	protected int maxAge() {
		return 300;
	}

	@Override
	protected double getDefaultGravity() {
		return 0.06;
	}
}
