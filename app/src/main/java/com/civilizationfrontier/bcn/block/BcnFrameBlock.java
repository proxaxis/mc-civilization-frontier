package com.civilizationfrontier.bcn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * サポートフレーム. ブロック面に貼り付く骨組みで、隣接フレーム同士が構造体を成す.
 * フレームモーターが構造体を動かすとき、フレームが取り付いているホストブロックも
 * 一緒に運ばれる (PR のフレーム駆動).
 */
public class BcnFrameBlock extends FaceAttachedHorizontalDirectionalBlock {

	private static final VoxelShape FLOOR_SHAPE = Block.box(0, 0, 0, 16, 4, 16);
	private static final VoxelShape CEILING_SHAPE = Block.box(0, 12, 0, 16, 16, 16);
	private static final VoxelShape WALL_NORTH_SHAPE = Block.box(0, 0, 0, 16, 16, 4);
	private static final VoxelShape WALL_SOUTH_SHAPE = Block.box(0, 0, 12, 16, 16, 16);
	private static final VoxelShape WALL_WEST_SHAPE = Block.box(0, 0, 0, 4, 16, 16);
	private static final VoxelShape WALL_EAST_SHAPE = Block.box(12, 0, 0, 16, 16, 16);

	public BcnFrameBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(
			net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACE, FACING);
	}

	/** フレームが取り付いているホストブロックの位置. */
	public static BlockPos hostPos(BlockState state, BlockPos pos) {
		return pos.relative(hostDirection(state));
	}

	public static Direction hostDirection(BlockState state) {
		return switch (state.getValue(FACE)) {
			case FLOOR -> Direction.DOWN;
			case CEILING -> Direction.UP;
			case WALL -> state.getValue(FACING).getOpposite();
		};
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACE)) {
			case FLOOR -> FLOOR_SHAPE;
			case CEILING -> CEILING_SHAPE;
			case WALL -> switch (state.getValue(FACING)) {
				case NORTH -> WALL_NORTH_SHAPE;
				case SOUTH -> WALL_SOUTH_SHAPE;
				case WEST -> WALL_WEST_SHAPE;
				case EAST -> WALL_EAST_SHAPE;
				default -> FLOOR_SHAPE;
			};
		};
	}
}
