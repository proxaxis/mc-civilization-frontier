package com.civilizationfrontier.bcn.block;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 小型論理回路素子. 床・壁・天井に貼れて、入出力端子 (周囲4方向) を持つ.
 * output 面からは弱/強両方の信号を出し、input 面は隣接ブロック・ワイヤーの信号を読む.
 */
public class BcnGateBlock extends FaceAttachedHorizontalDirectionalBlock {

	public enum GateType {
		NOT, BUFFER, OR, NOR, AND, NAND, XOR, XNOR, RS_LATCH, T_LATCH
	}

	// ローカル端子側ビット. 向きは FACING を基準に worldDir() でワールド方向へ変換する
	private static final int SIDE_FRONT = 1;
	private static final int SIDE_BACK = 2;
	private static final int SIDE_LEFT = 4;
	private static final int SIDE_RIGHT = 8;

	public static final BooleanProperty OUT_NORTH = BooleanProperty.create("out_north");
	public static final BooleanProperty OUT_SOUTH = BooleanProperty.create("out_south");
	public static final BooleanProperty OUT_EAST = BooleanProperty.create("out_east");
	public static final BooleanProperty OUT_WEST = BooleanProperty.create("out_west");
	public static final BooleanProperty OUT_UP = BooleanProperty.create("out_up");
	public static final BooleanProperty OUT_DOWN = BooleanProperty.create("out_down");

	private static final Map<Direction, BooleanProperty> OUT_BY_DIR = Map.of(
			Direction.NORTH, OUT_NORTH, Direction.SOUTH, OUT_SOUTH,
			Direction.EAST, OUT_EAST, Direction.WEST, OUT_WEST,
			Direction.UP, OUT_UP, Direction.DOWN, OUT_DOWN);

	// ラッチ系の内部状態. RS_LATCH: 0=リセット,1=FRONT側,2=BACK側. T_LATCH: bit0=出力側,bit1=直前入力
	public static final IntegerProperty MEM = IntegerProperty.create("mem", 0, 3);

	private final GateType type;
	private final boolean hasMemory;

	public BcnGateBlock(Properties properties, GateType type) {
		super(properties);
		this.type = type;
		this.hasMemory = type == GateType.RS_LATCH || type == GateType.T_LATCH;
		BlockState state = stateDefinition.any()
				.setValue(FACE, AttachFace.FLOOR)
				.setValue(FACING, Direction.NORTH);
		for (BooleanProperty p : OUT_BY_DIR.values()) {
			state = state.setValue(p, false);
		}
		if (hasMemory) {
			state = state.setValue(MEM, 0);
		}
		registerDefaultState(state);
	}

	public GateType gateType() {
		return type;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		// createBlockStateDefinition は super() 内で走るため hasMemory/type はまだ未設定.
		// MEM は常時登録し、非ラッチ系では読み書きしない
		builder.add(FACE, FACING, OUT_NORTH, OUT_SOUTH, OUT_EAST, OUT_WEST, OUT_UP, OUT_DOWN, MEM);
	}

	// ---- 端子レイアウト (ローカル側の入力/出力マスク) ----

	private int inputMask() {
		return switch (type) {
			case NOT, BUFFER -> SIDE_BACK;
			case OR, NOR, AND, NAND -> SIDE_LEFT | SIDE_RIGHT | SIDE_BACK;
			case XOR, XNOR, RS_LATCH, T_LATCH -> SIDE_LEFT | SIDE_RIGHT;
		};
	}

	private int outputMask() {
		return switch (type) {
			case NOT, BUFFER -> SIDE_FRONT | SIDE_LEFT | SIDE_RIGHT;
			case OR, NOR, AND, NAND, XOR, XNOR -> SIDE_FRONT;
			case RS_LATCH, T_LATCH -> SIDE_FRONT | SIDE_BACK;
		};
	}

	/** ローカル側をワールド方向へ. 壁面では FRONT=DOWN (信号は壁を下へ流す) */
	private Direction worldDir(BlockState state, int side) {
		Direction f = state.getValue(FACING);
		if (state.getValue(FACE) == AttachFace.WALL) {
			return switch (side) {
				case SIDE_FRONT -> Direction.DOWN;
				case SIDE_BACK -> Direction.UP;
				case SIDE_LEFT -> f.getClockWise();
				default -> f.getCounterClockWise();
			};
		}
		return switch (side) {
			case SIDE_FRONT -> f;
			case SIDE_BACK -> f.getOpposite();
			case SIDE_LEFT -> f.getCounterClockWise();
			default -> f.getClockWise();
		};
	}

	private int readInputs(Level level, BlockPos pos, BlockState state) {
		int in = 0;
		int mask = inputMask();
		for (int side : new int[]{SIDE_FRONT, SIDE_BACK, SIDE_LEFT, SIDE_RIGHT}) {
			if ((mask & side) == 0) {
				continue;
			}
			Direction dir = worldDir(state, side);
			if (level.getSignal(pos.relative(dir), dir) > 0) {
				in |= side;
			}
		}
		return in;
	}

	/** 入力とメモリから出力側ビットを評価し、新しい mem を outMem に書き戻す */
	private int evaluate(int in, int mem, int[] outMem) {
		switch (type) {
			case NOT -> {
				return (in & SIDE_BACK) == 0 ? outputMask() : 0;
			}
			case BUFFER -> {
				return (in & SIDE_BACK) != 0 ? outputMask() : 0;
			}
			case OR -> {
				return in != 0 ? outputMask() : 0;
			}
			case NOR -> {
				return in == 0 ? outputMask() : 0;
			}
			case AND -> {
				return in == inputMask() ? outputMask() : 0;
			}
			case NAND -> {
				return in != inputMask() ? outputMask() : 0;
			}
			case XOR -> {
				return ((in & SIDE_LEFT) != 0) != ((in & SIDE_RIGHT) != 0) ? outputMask() : 0;
			}
			case XNOR -> {
				return ((in & SIDE_LEFT) != 0) == ((in & SIDE_RIGHT) != 0) ? outputMask() : 0;
			}
			case RS_LATCH -> {
				boolean l = (in & SIDE_LEFT) != 0;
				boolean r = (in & SIDE_RIGHT) != 0;
				if (l && r) {
					mem = 0;
				} else if (l) {
					mem = 1;
				} else if (r) {
					mem = 2;
				}
				outMem[0] = mem;
				return mem == 1 ? SIDE_FRONT : mem == 2 ? SIDE_BACK : 0;
			}
			case T_LATCH -> {
				boolean inAny = in != 0;
				boolean prev = (mem & 2) != 0;
				int state = mem & 1;
				if (inAny && !prev) {
					state ^= 1;
				}
				outMem[0] = state | (inAny ? 2 : 0);
				return state == 0 ? SIDE_FRONT : SIDE_BACK;
			}
		}
		return 0;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int in = readInputs(level, pos, state);
		System.err.println("[BCN-DEBUG] tick " + type + " @" + pos + " in=" + in);
		int[] outMem = {hasMemory ? state.getValue(MEM) : 0};
		int out = evaluate(in, outMem[0], outMem);

		BlockState next = state;
		int mask = outputMask();
		for (int side : new int[]{SIDE_FRONT, SIDE_BACK, SIDE_LEFT, SIDE_RIGHT}) {
			if ((mask & side) == 0) {
				continue;
			}
			next = next.setValue(OUT_BY_DIR.get(worldDir(state, side)), (out & side) != 0);
		}
		if (hasMemory) {
			next = next.setValue(MEM, outMem[0]);
		}
		if (next != state) {
			level.setBlock(pos, next, Block.UPDATE_ALL);
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
		if (!level.isClientSide()) {
			System.err.println("[BCN-DEBUG] neighborChanged " + type + " @" + pos);
			level.scheduleTick(pos, this, 2);
		}
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!level.isClientSide()) {
			System.err.println("[BCN-DEBUG] onPlace " + type + " @" + pos);
			level.scheduleTick(pos, this, 2);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (type == GateType.T_LATCH) {
			if (!level.isClientSide()) {
				level.setBlock(pos, state.setValue(MEM, state.getValue(MEM) ^ 1), Block.UPDATE_ALL);
				level.scheduleTick(pos, this, 1);
				level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.6F);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useWithoutItem(state, level, pos, player, hit);
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return outputAt(state, direction.getOpposite()) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return getSignal(state, level, pos, direction);
	}

	private boolean outputAt(BlockState state, Direction dir) {
		return state.getValue(OUT_BY_DIR.get(dir));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACE)) {
			case FLOOR -> Block.box(0, 0, 0, 16, 3, 16);
			case CEILING -> Block.box(0, 13, 0, 16, 16, 16);
			case WALL -> switch (state.getValue(FACING)) {
				case NORTH -> Block.box(0, 0, 0, 16, 16, 3);
				case SOUTH -> Block.box(0, 0, 13, 16, 16, 16);
				case WEST -> Block.box(0, 0, 0, 3, 16, 16);
				case EAST -> Block.box(13, 0, 0, 16, 16, 16);
				default -> Block.box(0, 0, 0, 16, 3, 16);
			};
		};
	}
}
