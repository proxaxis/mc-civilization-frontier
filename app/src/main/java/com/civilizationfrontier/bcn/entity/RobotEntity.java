package com.civilizationfrontier.bcn.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** ロボット. docking_station に接続して待機する最小実装 (移動/AI は別タスク). */
public class RobotEntity extends Entity {

	public RobotEntity(EntityType<?> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
			net.minecraft.world.damagesource.DamageSource source, float amount) {
		return false;
	}
}
