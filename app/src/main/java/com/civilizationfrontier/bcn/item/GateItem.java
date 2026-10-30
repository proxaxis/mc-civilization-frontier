package com.civilizationfrontier.bcn.item;

import com.civilizationfrontier.bcn.block.entity.ItemPipeBlockEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/**
 * BC 式ゲート. パイプに右クリックで装着し、レッドストーン条件で抽出を制御する.
 * AND: 信号ONで抽出許可 / OR: 信号OFFで抽出許可 (redwire 論理系とは別系統).
 * 素手右クリックで取り外しはパイプ側が扱う.
 */
public class GateItem extends Item {

	public enum Kind { AND, OR }

	private final Kind kind;

	public GateItem(Kind kind, Properties properties) {
		super(properties);
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof ItemPipeBlockEntity pipe) {
			if (!ctx.getLevel().isClientSide()) {
				pipe.setGate(kind == Kind.AND ? "and" : "or");
				ctx.getLevel().playSound(null, ctx.getClickedPos(), SoundEvents.ITEM_FRAME_ADD_ITEM,
						SoundSource.BLOCKS, 1.0F, 1.0F);
				if (ctx.getPlayer() != null && !ctx.getPlayer().getAbilities().instabuild) {
					ctx.getItemInHand().shrink(1);
				}
			}
			return InteractionResult.SUCCESS;
		}
		return super.useOn(ctx);
	}
}
