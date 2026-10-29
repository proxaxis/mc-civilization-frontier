package com.civilizationfrontier.bcn.item;

import com.civilizationfrontier.bcn.energy.MjReceiver;
import com.civilizationfrontier.bcn.energy.block.entity.MjStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 検流計 (multimeter/voltmeter). MJ ブロックを右クリックすると
 * 蓄電量・累計送受電をチャット表示する (blulectricity 側の計測具).
 */
public class BcnMultimeterItem extends Item {

	public BcnMultimeterItem(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockEntity be = level.getBlockEntity(pos);
		Player player = ctx.getPlayer();
		if (!(be instanceof MjReceiver)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide() && player != null) {
			if (be instanceof MjStorageBlockEntity store) {
				player.sendOverlayMessage(Component.literal(String.format(
						"%.1f MJ stored (in %.1f / out %.1f)",
						store.energy(), store.totalReceived(), store.totalEmitted())));
			} else {
				player.sendOverlayMessage(Component.literal("MJ receiver"));
			}
		}
		return InteractionResult.SUCCESS;
	}
}
