package com.civilizationfrontier.bcn.block;

import java.util.Map;

import com.civilizationfrontier.bcn.block.entity.BundledCableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * ケーブル束 (bundled cable). 16 チャネルの色付き信号を 1 本で運ぶ太い配線.
 * 各色の絶縁ワイヤーがタップとして接続し、チャネル状態は BlockEntity の
 * 16bit マスクとして保持される. 通常ブロックへは信号を出さない.
 */
public class BcnBundledCableBlock extends FaceAttachedHorizontalDirectionalBlock implements EntityBlock {

	public static final EnumProperty<BcnWireBlock.Conn> NORTH = EnumProperty.create("north", BcnWireBlock.Conn.class);
	public static final EnumProperty<BcnWireBlock.Conn> SOUTH = EnumProperty.create("south", BcnWireBlock.Conn.class);
	public static final EnumProperty<BcnWireBlock.Conn> EAST = EnumProperty.create("east", BcnWireBlock.Conn.class);
	public static final EnumProperty<BcnWireBlock.Conn> WEST = EnumProperty.create("west", BcnWireBlock.Conn.class);
	public static final EnumProperty<BcnWireBlock.Conn> UP = EnumProperty.create("up", BcnWireBlock.Conn.class);
	public static final EnumProperty<BcnWireBlock.Conn> DOWN = EnumProperty.create("down", BcnWireBlock.Conn.class);

	private static final Map<Direction, EnumProperty<BcnWireBlock.Conn>> CONN_BY_DIR = Map.of(
			Direction.NORTH, NORTH, Direction.SOUTH, SOUTH,
			Direction.EAST, EAST, Direction.WEST, WEST,
			Direction.UP, UP, Direction.DOWN, DOWN);

	private static final VoxelShape FLOOR_SHAPE = Block.box(0, 0, 0, 16, 5, 16);
	private static final VoxelShape CEILING_SHAPE = Block.box(0, 11, 0, 16, 16, 16);
	private static final VoxelShape WALL_NORTH_SHAPE = Block.box(0, 0, 0, 16, 16, 5);
	private static final VoxelShape WALL_SOUTH_SHAPE = Block.box(0, 0, 11, 16, 16, 16);
	private static final VoxelShape WALL_WEST_SHAPE = Block.box(0, 0, 0, 5, 16, 16);
	private static final VoxelShape WALL_EAST_SHAPE = Block.box(11, 0, 0, 16, 16, 16);

	public BcnBundledCableBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(FACE, AttachFace.FLOOR)
				.setValue(FACING, Direction.NORTH)
				.setValue(NORTH, BcnWireBlock.Conn.NONE).setValue(SOUTH, BcnWireBlock.Conn.NONE)
				.setValue(EAST, BcnWireBlock.Conn.NONE).setValue(WEST, BcnWireBlock.Conn.NONE)
				.setValue(UP, BcnWireBlock.Conn.NONE).setValue(DOWN, BcnWireBlock.Conn.NONE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACE, FACING, NORTH, SOUTH, EAST, WEST, UP, DOWN);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BundledCableBlockEntity(pos, state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState state = super.getStateForPlacement(ctx);
		if (state == null) {
			return null;
		}
		return updateConnections(state, ctx.getLevel(), ctx.getClickedPos());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
			BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
		state = super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
		if (state.isAir()) {
			return state;
		}
		return state.setValue(CONN_BY_DIR.get(direction), connTo(neighbor));
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!level.isClientSide()) {
			ColoredWireNetwork.updateNetwork(level, pos);
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
			BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		if (!level.isClientSide()) {
			ColoredWireNetwork.updateNetwork(level, pos);
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
		if (!level.isClientSide()) {
			ColoredWireNetwork.updateNetwork(level, pos);
		}
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

	private BlockState updateConnections(BlockState state, LevelReader level, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			state = state.setValue(CONN_BY_DIR.get(dir), connTo(level.getBlockState(pos.relative(dir))));
		}
		return state;
	}

	private static BcnWireBlock.Conn connTo(BlockState neighbor) {
		if (neighbor.getBlock() instanceof BcnBundledCableBlock
				|| neighbor.getBlock() instanceof BcnInsulatedWireBlock) {
			return BcnWireBlock.Conn.WIRE;
		}
		return BcnWireBlock.Conn.NONE;
	}
}
