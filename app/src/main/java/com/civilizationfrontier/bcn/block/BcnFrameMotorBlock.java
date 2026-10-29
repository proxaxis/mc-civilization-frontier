package com.civilizationfrontier.bcn.block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;

/**
 * フレームモーター. RS 信号で稼働し、隣接するフレーム構造体
 * (フレーム + フレームが取り付いたホストブロック) を FACING 方向へ 1 ブロックずつ移す.
 * 移動は瞬間的 (アニメーションなし)、ホストの BlockEntity は NBT で引き継ぐ.
 * 最小スコープ: 構造体サイズ上限 {@value #MAX_MOVE} ブロック.
 */
public class BcnFrameMotorBlock extends Block {

	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	private static final int MAX_MOVE = 512;
	private static final int MOVE_INTERVAL = 4;

	public BcnFrameMotorBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(FACING, Direction.NORTH).setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection());
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
		boolean powered = level.hasNeighborSignal(pos);
		if (powered == state.getValue(POWERED)) {
			return;
		}
		level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
		if (powered && !level.isClientSide()) {
			level.scheduleTick(pos, this, MOVE_INTERVAL);
		}
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!level.isClientSide() && level.hasNeighborSignal(pos)) {
			level.scheduleTick(pos, this, MOVE_INTERVAL);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.getValue(POWERED)) {
			return;
		}
		moveStructure(level, pos, state);
		if (level.getBlockState(pos).is(this) && level.getBlockState(pos).getValue(POWERED)) {
			level.scheduleTick(pos, this, MOVE_INTERVAL);
		}
	}

	/** モーターに接するフレーム構造体を移動方向へ1ブロックずらす. */
	private void moveStructure(Level level, BlockPos pos, BlockState state) {
		Direction dir = state.getValue(FACING);
		Set<BlockPos> move = collectMoveSet(level, pos);
		if (move.isEmpty() || move.size() > MAX_MOVE) {
			return;
		}
		for (BlockPos p : move) {
			BlockPos target = p.relative(dir);
			if (move.contains(target)) {
				continue;
			}
			BlockState ts = level.getBlockState(target);
			if (!ts.isAir() && !ts.canBeReplaced()) {
				return; // 行き先が塞がれている
			}
			if (level.getBlockState(p).getPistonPushReaction() == PushReaction.IMMOVEABLE) {
				return; // 移動不能ブロックを含む
			}
		}

		List<BlockPos> ordered = new ArrayList<>(move);
		int sign = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? -1 : 1;
		ordered.sort(Comparator.comparingInt(p -> sign * axis(p, dir.getAxis())));

		Map<BlockPos, CompoundTag> beTags = new HashMap<>();
		for (BlockPos p : ordered) {
			BlockEntity be = level.getBlockEntity(p);
			if (be != null) {
				beTags.put(p, be.saveWithFullMetadata(level.registryAccess()));
			}
		}
		Map<BlockPos, BlockState> states = new HashMap<>();
		for (BlockPos p : ordered) {
			states.put(p, level.getBlockState(p));
			level.removeBlockEntity(p);
			level.setBlock(p, Blocks.AIR.defaultBlockState(),
					Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		}
		for (BlockPos p : ordered) {
			BlockPos target = p.relative(dir);
			level.setBlock(target, states.get(p),
					Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_MOVE_BY_PISTON);
			CompoundTag tag = beTags.get(p);
			if (tag != null) {
				tag.putInt("x", target.getX());
				tag.putInt("y", target.getY());
				tag.putInt("z", target.getZ());
				BlockEntity nbe = BlockEntity.loadStatic(target, states.get(p), tag, level.registryAccess());
				if (nbe != null) {
					level.setBlockEntity(nbe);
				}
			}
		}
		for (BlockPos p : ordered) {
			level.updateNeighborsAt(p, level.getBlockState(p).getBlock());
			BlockPos target = p.relative(dir);
			level.updateNeighborsAt(target, level.getBlockState(target).getBlock());
		}
	}

	/** モーター面に接するフレームを種に、フレーム網 + ホストブロック + ホスト上の全フレームを集める. */
	private Set<BlockPos> collectMoveSet(Level level, BlockPos motorPos) {
		Set<BlockPos> frames = new HashSet<>();
		Deque<BlockPos> open = new ArrayDeque<>();
		for (Direction d : Direction.values()) {
			BlockPos q = motorPos.relative(d);
			if (level.getBlockState(q).getBlock() instanceof BcnFrameBlock) {
				open.add(q);
			}
		}
		while (!open.isEmpty()) {
			BlockPos p = open.poll();
			if (!frames.add(p)) {
				continue;
			}
			for (Direction d : Direction.values()) {
				BlockPos q = p.relative(d);
				if (!frames.contains(q) && level.getBlockState(q).getBlock() instanceof BcnFrameBlock) {
					open.add(q);
				}
			}
		}
		if (frames.isEmpty()) {
			return Set.of();
		}
		Set<BlockPos> move = new HashSet<>(frames);
		for (BlockPos f : frames) {
			move.add(BcnFrameBlock.hostPos(level.getBlockState(f), f));
		}
		// ホストの別面に貼られたフレームも一緒に運ぶ
		List<BlockPos> hosts = new ArrayList<>(move);
		for (BlockPos h : hosts) {
			for (Direction d : Direction.values()) {
				BlockPos q = h.relative(d);
				BlockState qs = level.getBlockState(q);
				if (qs.getBlock() instanceof BcnFrameBlock
						&& BcnFrameBlock.hostPos(qs, q).equals(h)) {
					move.add(q);
				}
			}
		}
		return move;
	}

	private static int axis(BlockPos p, Direction.Axis axis) {
		return switch (axis) {
			case X -> p.getX();
			case Y -> p.getY();
			case Z -> p.getZ();
		};
	}
}
