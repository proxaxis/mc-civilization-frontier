package com.civilizationfrontier.bcn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;

public class BcnEngineBlock extends Block {
	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public BcnEngineBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ACTIVE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState()
				.setValue(FACING, ctx.getNearestLookingDirection().getOpposite())
				.setValue(ACTIVE, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			Orientation orientation, boolean movedByPiston) {
		if (!level.isClientSide()) {
			boolean powered = level.hasNeighborSignal(pos);
			if (state.getValue(ACTIVE) != powered) {
				level.setBlock(pos, state.setValue(ACTIVE, powered), Block.UPDATE_ALL);
			}
		}
	}
}
