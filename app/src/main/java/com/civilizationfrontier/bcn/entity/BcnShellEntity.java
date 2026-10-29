package com.civilizationfrontier.bcn.entity;

import com.civilizationfrontier.bcn.energy.BcnEfp;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 重火器の砲弾基底. 低負荷設計:
 * - noSave なのでチャンクアンロード/再起動で滞留しない
 * - maxAge を超えると着弾せず消滅し、tick コストに上限を設ける
 * - 同時存在数は BcnEntities.hasShellCapacity が上限管理する
 */
public abstract class BcnShellEntity extends ThrowableItemProjectile {

	protected BcnShellEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
		super(type, level);
	}

	protected BcnShellEntity(EntityType<? extends ThrowableItemProjectile> type, Level level, Vec3 pos) {
		this(type, level);
		setPos(pos);
	}

	protected abstract float explosionPower();

	protected abstract long impactEfp();

	protected abstract int maxAge();

	@Override
	public void tick() {
		super.tick();
		if (tickCount > maxAge()) {
			discard();
		}
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.explode(this, getX(), getY(), getZ(), explosionPower(), false,
					Level.ExplosionInteraction.TNT);
			BcnEfp.add(serverLevel.getServer(), impactEfp());
			discard();
		}
	}

	@Override
	public boolean isPickable() {
		return false;
	}
}
