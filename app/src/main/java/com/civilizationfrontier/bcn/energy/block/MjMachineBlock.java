package com.civilizationfrontier.bcn.energy.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * MJ 機械ブロック基底. FACING = 出力面、ACTIVE = 稼働表示.
 * エネルギーは FACING 面からのみ流出し、残り5面が入力面となる.
 */
public abstract class MjMachineBlock extends Block implements EntityBlock {

	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	protected MjMachineBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(FACING, Direction.UP).setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ACTIVE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState()
				.setValue(FACING, ctx.getNearestLookingDirection().getOpposite())
				.setValue(ACTIVE, false);
	}

	@SuppressWarnings("unchecked")
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> ticker(
			BlockEntityType<A> found, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker) {
		return expected == found ? (BlockEntityTicker<A>) ticker : null;
	}
}
