package com.civilizationfrontier.bcn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

/** レンチ. 右クリックで facing プロパティを持つブロックを回転する (BC レンチ準拠). */
public class BcnWrenchItem extends Item {

	public BcnWrenchItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = level.getBlockState(pos);
		for (Property<?> property : state.getProperties()) {
			if (!"facing".equals(property.getName())) {
				continue;
			}
			if (!(property instanceof EnumProperty<?> ep) || ep.getValueClass() != Direction.class) {
				continue;
			}
			if (!level.isClientSide()) {
				Direction current = (Direction) state.getValue(ep);
				Direction next = Direction.values()[(current.ordinal() + 1) % Direction.values().length];
				@SuppressWarnings({"unchecked", "rawtypes"})
				BlockState rotated = state.setValue((EnumProperty) ep, next);
				level.setBlock(pos, rotated, Block.UPDATE_ALL);
				level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useOn(ctx);
	}
}
