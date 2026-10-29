package com.civilizationfrontier.bcn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.RandomSource;

public class BcnPipeBlock extends Block {

	public enum Conn implements StringRepresentable {
		NONE, PIPE, BLOCK;
		@Override public String getSerializedName() { return name().toLowerCase(); }
	}

	public static final EnumProperty<Conn> NORTH = EnumProperty.create("north", Conn.class);
	public static final EnumProperty<Conn> SOUTH = EnumProperty.create("south", Conn.class);
	public static final EnumProperty<Conn> EAST = EnumProperty.create("east", Conn.class);
	public static final EnumProperty<Conn> WEST = EnumProperty.create("west", Conn.class);
	public static final EnumProperty<Conn> UP = EnumProperty.create("up", Conn.class);
	public static final EnumProperty<Conn> DOWN = EnumProperty.create("down", Conn.class);

	public BcnPipeBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(NORTH, Conn.NONE).setValue(SOUTH, Conn.NONE)
				.setValue(EAST, Conn.NONE).setValue(WEST, Conn.NONE)
				.setValue(UP, Conn.NONE).setValue(DOWN, Conn.NONE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return updateState(defaultBlockState(), ctx.getLevel(), ctx.getClickedPos());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
			BlockPos pos, Direction dir, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
		return updateState(state, level, pos);
	}

	private BlockState updateState(BlockState state, LevelReader level, BlockPos pos) {
		return state
				.setValue(NORTH, conn(level.getBlockState(pos.north())))
				.setValue(SOUTH, conn(level.getBlockState(pos.south())))
				.setValue(EAST, conn(level.getBlockState(pos.east())))
				.setValue(WEST, conn(level.getBlockState(pos.west())))
				.setValue(UP, conn(level.getBlockState(pos.above())))
				.setValue(DOWN, conn(level.getBlockState(pos.below())));
	}

	private Conn conn(BlockState neighbor) {
		if (neighbor.getBlock() instanceof BcnPipeBlock) return Conn.PIPE;
		if (neighbor.hasBlockEntity()) return Conn.BLOCK; // 容器・機械のみ終端接続
		return Conn.NONE;
	}
}
