package com.civilizationfrontier.bcn.item;

import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.civilizationfrontier.bcn.entity.BcnEntities;
import com.civilizationfrontier.bcn.entity.RobotEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** ロボット. docking_station へ右クリックでドッキング配置する. */
public class RobotItem extends Item {

	public RobotItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		if (level.getBlockState(ctx.getClickedPos()).getBlock() != BcnBlocks.DOCKING_STATION) {
			return super.useOn(ctx);
		}
		if (!level.isClientSide()) {
			var existing = level.getEntities(BcnEntities.ROBOT,
					new net.minecraft.world.phys.AABB(ctx.getClickedPos()).inflate(1.0), e -> true);
			if (existing.isEmpty()) {
				RobotEntity robot = new RobotEntity(BcnEntities.ROBOT, level);
				robot.setPos(ctx.getClickedPos().getX() + 0.5,
						ctx.getClickedPos().getY() + 1.0, ctx.getClickedPos().getZ() + 0.5);
				level.addFreshEntity(robot);
				if (ctx.getPlayer() != null && !ctx.getPlayer().getAbilities().instabuild) {
					ctx.getItemInHand().shrink(1);
				}
			}
		}
		return InteractionResult.SUCCESS;
	}
}
