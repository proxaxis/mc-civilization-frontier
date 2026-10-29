package com.civilizationfrontier.bcn.block;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 壁・床・天井に貼れる RS ワイヤー. BcnPipeBlock と同じ multipart 接続モデルで
 * 見た目の接続を管理し、同一コンポーネント内で RS 信号を共通伝播する.
 */
public class BcnWireBlock extends FaceAttachedHorizontalDirectionalBlock {

	public enum Conn implements StringRepresentable {
		NONE, WIRE, BLOCK;

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}
	}

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final EnumProperty<Conn> NORTH = EnumProperty.create("north", Conn.class);
	public static final EnumProperty<Conn> SOUTH = EnumProperty.create("south", Conn.class);
	public static final EnumProperty<Conn> EAST = EnumProperty.create("east", Conn.class);
	public static final EnumProperty<Conn> WEST = EnumProperty.create("west", Conn.class);
	public static final EnumProperty<Conn> UP = EnumProperty.create("up", Conn.class);
	public static final EnumProperty<Conn> DOWN = EnumProperty.create("down", Conn.class);

	private static final Map<Direction, EnumProperty<Conn>> CONN_BY_DIR = Map.of(
			Direction.NORTH, NORTH, Direction.SOUTH, SOUTH,
			Direction.EAST, EAST, Direction.WEST, WEST,
			Direction.UP, UP, Direction.DOWN, DOWN);

	// 信号が伝播する近傍. 面6 + 辺12 (壁の角を回り込めるように斜め接続も許可)
	private static final BlockPos[] NEIGHBOR_OFFSETS = buildNeighborOffsets();

	private static BlockPos[] buildNeighborOffsets() {
		java.util.List<BlockPos> list = new java.util.ArrayList<>();
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				for (int dz = -1; dz <= 1; dz++) {
					int m = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
					if (m == 1 || m == 2) {
						list.add(new BlockPos(dx, dy, dz));
					}
				}
			}
		}
		return list.toArray(new BlockPos[0]);
	}

	private static final VoxelShape FLOOR_SHAPE = Block.box(0, 0, 0, 16, 2, 16);
	private static final VoxelShape CEILING_SHAPE = Block.box(0, 14, 0, 16, 16, 16);
	private static final VoxelShape WALL_NORTH_SHAPE = Block.box(0, 0, 0, 16, 16, 2);
	private static final VoxelShape WALL_SOUTH_SHAPE = Block.box(0, 0, 14, 16, 16, 16);
	private static final VoxelShape WALL_WEST_SHAPE = Block.box(0, 0, 0, 2, 16, 16);
	private static final VoxelShape WALL_EAST_SHAPE = Block.box(14, 0, 0, 16, 16, 16);

	public BcnWireBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(FACE, AttachFace.FLOOR)
				.setValue(FACING, Direction.NORTH)
				.setValue(POWERED, false)
				.setValue(NORTH, Conn.NONE).setValue(SOUTH, Conn.NONE)
				.setValue(EAST, Conn.NONE).setValue(WEST, Conn.NONE)
				.setValue(UP, Conn.NONE).setValue(DOWN, Conn.NONE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACE, FACING, POWERED, NORTH, SOUTH, EAST, WEST, UP, DOWN);
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
			updateNetwork(level, pos);
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
			BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		if (!level.isClientSide()) {
			updateNetwork(level, pos);
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
		if (!level.isClientSide()) {
			updateNetwork(level, pos);
		}
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.getValue(POWERED) ? 15 : 0;
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

	private static Conn connTo(BlockState neighbor) {
		if (neighbor.getBlock() instanceof BcnWireBlock) {
			return Conn.WIRE;
		}
		if (neighbor.getBlock() instanceof BcnGateBlock || neighbor.getBlock() instanceof BcnEngineBlock
				|| neighbor.isSignalSource()) {
			return Conn.BLOCK;
		}
		return Conn.NONE;
	}

	// ---- 信号伝播 ----

	/**
	 * pos 周辺のワイヤーコンポーネントを再評価し powered を一括更新する.
	 * pos がワイヤーでなくても近傍のワイヤーを種に使うので、撤去後の呼び出しにも対応する.
	 */
	public static void updateNetwork(Level level, BlockPos pos) {
		Set<BlockPos> visited = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		seedAt(level, pos, queue);
		for (BlockPos off : NEIGHBOR_OFFSETS) {
			seedAt(level, pos.offset(off.getX(), off.getY(), off.getZ()), queue);
		}
		while (!queue.isEmpty()) {
			BlockPos seed = queue.poll();
			if (!visited.contains(seed)) {
				updateComponent(level, seed, visited, queue);
			}
		}
	}

	private static void seedAt(Level level, BlockPos pos, Deque<BlockPos> queue) {
		if (level.getBlockState(pos).getBlock() instanceof BcnWireBlock) {
			queue.add(pos);
		}
	}

	private static void updateComponent(Level level, BlockPos seed, Set<BlockPos> visited, Deque<BlockPos> queue) {
		Set<BlockPos> members = new HashSet<>();
		Deque<BlockPos> open = new ArrayDeque<>();
		open.add(seed);
		while (!open.isEmpty()) {
			BlockPos p = open.poll();
			if (!members.add(p)) {
				continue;
			}
			for (BlockPos off : NEIGHBOR_OFFSETS) {
				BlockPos q = p.offset(off.getX(), off.getY(), off.getZ());
				if (!members.contains(q) && !visited.contains(q)
						&& level.getBlockState(q).getBlock() instanceof BcnWireBlock) {
					open.add(q);
				}
			}
		}
		boolean powered = false;
		for (BlockPos p : members) {
			if (externalSignal(level, p) > 0) {
				powered = true;
				break;
			}
		}
		for (BlockPos p : members) {
			BlockState s = level.getBlockState(p);
			if (s.getValue(POWERED) != powered) {
				level.setBlock(p, s.setValue(POWERED, powered), Block.UPDATE_ALL);
			}
		}
		visited.addAll(members);
	}

	/** ワイヤー/ダスト以外の外部ソースが発する信号の最大値を読む (フィードバックループ防止). */
	private static int externalSignal(Level level, BlockPos pos) {
		int signal = 0;
		for (Direction dir : Direction.values()) {
			BlockPos np = pos.relative(dir);
			BlockState ns = level.getBlockState(np);
			if (ns.getBlock() instanceof BcnWireBlock || ns.getBlock() instanceof RedstoneWireBlock) {
				continue;
			}
			signal = Math.max(signal, level.getSignal(np, dir));
			if (signal >= 15) {
				break;
			}
		}
		return signal;
	}
}
