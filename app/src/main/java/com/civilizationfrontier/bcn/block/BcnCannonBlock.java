package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.entity.BcnShellEntity;
import com.civilizationfrontier.bcn.entity.CannonballEntity;
import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

/** 大砲. 向いている方向へ高速の砲弾を直進発射する. */
public class BcnCannonBlock extends BcnArtilleryBlock {

	public BcnCannonBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected Item ammoItem() {
		return BcnItems.CANNONBALL;
	}

	@Override
	protected BcnShellEntity createShell(ServerLevel level, Vec3 muzzle) {
		return new CannonballEntity(level, muzzle);
	}

	@Override
	protected Vec3 launchVector(Direction facing) {
		return facing.getUnitVec3().add(0.0, 0.02, 0.0);
	}

	@Override
	protected Vec3 muzzleOffset(Direction facing) {
		return facing.getUnitVec3().scale(0.9);
	}

	@Override
	protected float velocity() {
		return 3.5F;
	}

	@Override
	protected float inaccuracy() {
		return 0.02F;
	}

	@Override
	protected long fireEfp() {
		return 4L;
	}
}
