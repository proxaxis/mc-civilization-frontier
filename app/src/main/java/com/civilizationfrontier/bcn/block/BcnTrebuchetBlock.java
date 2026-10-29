package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.entity.BcnShellEntity;
import com.civilizationfrontier.bcn.entity.SiegeShotEntity;
import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

/** 投石機(攻城兵器). 巨石を放物線で投擲し、着弾で大きく破壊する. */
public class BcnTrebuchetBlock extends BcnArtilleryBlock {

	public BcnTrebuchetBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected Item ammoItem() {
		return BcnItems.SIEGE_SHOT;
	}

	@Override
	protected BcnShellEntity createShell(ServerLevel level, Vec3 muzzle) {
		return new SiegeShotEntity(level, muzzle);
	}

	@Override
	protected Vec3 launchVector(Direction facing) {
		return facing.getUnitVec3().scale(0.75).add(0.0, 0.85, 0.0);
	}

	@Override
	protected Vec3 muzzleOffset(Direction facing) {
		return facing.getUnitVec3().scale(0.5).add(0.0, 1.3, 0.0);
	}

	@Override
	protected float velocity() {
		return 2.0F;
	}

	@Override
	protected float inaccuracy() {
		return 0.05F;
	}

	@Override
	protected long fireEfp() {
		return 8L;
	}
}
