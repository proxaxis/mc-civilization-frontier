package com.civilizationfrontier.bcn.block;

import java.util.Map;

import com.civilizationfrontier.bcn.block.entity.IcGateBlockEntity;
import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.network.chat.Component;

/**
 * 集積回路ゲート. 1 ブロックの中に最大 {@value IcGateBlockEntity#MAX_CELLS} 個の
 * 論理セルを記憶し、それぞれがローカル端子 (周囲4方向) との入出力マスクを持つ.
 *
 * - ゲートアイテムを持って右クリック: セルを末尾に追加 (デフォルト端子配置)
 * - ドライバーで右クリック: 末尾のセルを取り外す
 * - 素手で右クリック: 現在のセル構成をチャットに表示
 * - 高度な配線 (セル連鎖 src) は NBT (data コマンド) で書き込める
 */
public class BcnIcGateBlock extends FaceAttachedHorizontalDirectionalBlock implements EntityBlock {

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

	private static final int SIDE_FRONT = IcGateBlockEntity.SIDE_FRONT;
	private static final int SIDE_BACK = IcGateBlockEntity.SIDE_BACK;
	private static final int SIDE_LEFT = IcGateBlockEntity.SIDE_LEFT;
	private static final int SIDE_RIGHT = IcGateBlockEntity.SIDE_RIGHT;

	public BcnIcGateBlock(Properties properties) {
		super(properties);
		BlockState state = stateDefinition.any()
				.setValue(FACE, AttachFace.FLOOR)
				.setValue(FACING, Direction.NORTH);
		for (BooleanProperty p : OUT_BY_DIR.values()) {
			state = state.setValue(p, false);
		}
		registerDefaultState(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACE, FACING, OUT_NORTH, OUT_SOUTH, OUT_EAST, OUT_WEST, OUT_UP, OUT_DOWN);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new IcGateBlockEntity(pos, state);
	}

	/** ローカル側をワールド方向へ ({@link BcnGateBlock} と同一規則). */
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
		for (int side : new int[]{SIDE_FRONT, SIDE_BACK, SIDE_LEFT, SIDE_RIGHT}) {
			Direction dir = worldDir(state, side);
			if (level.getSignal(pos.relative(dir), dir) > 0) {
				in |= side;
			}
		}
		return in;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!(level.getBlockEntity(pos) instanceof IcGateBlockEntity be)) {
			return;
		}
		int out = be.evaluate(readInputs(level, pos, state));
		BlockState next = state;
		for (int side : new int[]{SIDE_FRONT, SIDE_BACK, SIDE_LEFT, SIDE_RIGHT}) {
			next = next.setValue(OUT_BY_DIR.get(worldDir(state, side)), (out & side) != 0);
		}
		if (next != state) {
			level.setBlock(pos, next, Block.UPDATE_ALL);
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
		if (!level.isClientSide()) {
			level.scheduleTick(pos, this, 2);
		}
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!level.isClientSide()) {
			level.scheduleTick(pos, this, 2);
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof IcGateBlockEntity be)) {
			return InteractionResult.SUCCESS;
		}
		if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof BcnGateBlock gate) {
			if (be.addCell(gate.gateType())) {
				level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, 0.6F);
				player.sendOverlayMessage(Component.literal(
						"IC cell " + be.cells().size() + "/" + IcGateBlockEntity.MAX_CELLS + ": " + gate.gateType()));
			} else {
				player.sendOverlayMessage(Component.literal("IC cells full (" + IcGateBlockEntity.MAX_CELLS + ")"));
			}
			return InteractionResult.SUCCESS;
		}
		if (stack.is(BcnItems.SCREWDRIVER)) {
			IcGateBlockEntity.Cell removed = be.removeLastCell();
			if (removed != null) {
				level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_OFF, SoundSource.BLOCKS, 0.3F, 0.5F);
				player.sendOverlayMessage(Component.literal("removed cell: " + removed.type()));
				level.scheduleTick(pos, this, 1);
			} else {
				player.sendOverlayMessage(Component.literal("no cells"));
			}
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof IcGateBlockEntity be) {
			if (be.cells().isEmpty()) {
				player.sendOverlayMessage(Component.literal("IC gate: empty (click with a gate to add)"));
			} else {
				StringBuilder sb = new StringBuilder("IC cells:");
				int i = 0;
				for (IcGateBlockEntity.Cell c : be.cells()) {
					sb.append(' ').append(i++).append('=').append(c.type())
							.append("[in:").append(c.inMask()).append(" out:").append(c.outMask())
							.append(" src:").append(c.src()).append(']');
				}
				player.sendOverlayMessage(Component.literal(sb.toString()));
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.getValue(OUT_BY_DIR.get(direction.getOpposite())) ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return getSignal(state, level, pos, direction);
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
