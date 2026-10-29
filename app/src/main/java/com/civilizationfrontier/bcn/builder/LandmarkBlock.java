package com.civilizationfrontier.bcn.builder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 建築範囲指定マーカー (landmark=青, path_mark=緑). 右クリックで active 切替.
 * 同一直線上 (軸方向) のマーカー同士が連結し、3 個以上で直方体範囲を形成する.
 */
public class LandmarkBlock extends Block {

	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final int MAX_DISTANCE = 64;

	/** 見た目だけの区別用. PATH では範囲形成に参加しない */
	private final boolean path;

	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(6, 0, 6, 10, 8, 10),
			Block.box(4, 8, 4, 12, 12, 12));

	public LandmarkBlock(Properties properties, boolean path) {
		super(properties);
		this.path = path;
		registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
	}

	public boolean isPath() {
		return path;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		boolean active = !state.getValue(ACTIVE);
		level.setBlock(pos, state.setValue(ACTIVE, active), Block.UPDATE_ALL);
		if (active && !path) {
			BlockVolume vol = findVolume(level, pos);
			if (vol != null) {
				player.sendSystemMessage(Component.literal(
						"Volume: " + vol.sizeX() + "x" + vol.sizeY() + "x" + vol.sizeZ()
								+ " @ " + vol.minX() + "," + vol.minY() + "," + vol.minZ()));
			} else {
				player.sendSystemMessage(Component.literal("No volume: needs 2+ connected landmarks on an axis"));
			}
		}
		return InteractionResult.SUCCESS;
	}

	/** 軸方向に最近のマーカーを探す (MAX_DISTANCE 以内). */
	private static BlockPos scanAxis(Level level, BlockPos from, Direction dir) {
		BlockPos.MutableBlockPos p = from.mutable();
		for (int i = 0; i < MAX_DISTANCE; i++) {
			p.move(dir);
			BlockState s = level.getBlockState(p);
			if (s.getBlock() instanceof LandmarkBlock) {
				return p.immutable();
			}
			if (s.isRedstoneConductor(level, p)) {
				return null;
			}
		}
		return null;
	}

	/**
	 * 直方体範囲 (min/max の両端含む). path マーカーは含めない.
	 */
	public static final class BlockVolume {
		private final int minX, minY, minZ, maxX, maxY, maxZ;

		BlockVolume(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
			this.minX = minX; this.minY = minY; this.minZ = minZ;
			this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
		}

		public int minX() { return minX; }
		public int minY() { return minY; }
		public int minZ() { return minZ; }
		public int maxX() { return maxX; }
		public int maxY() { return maxY; }
		public int maxZ() { return maxZ; }
		public int sizeX() { return maxX - minX + 1; }
		public int sizeY() { return maxY - minY + 1; }
		public int sizeZ() { return maxZ - minZ + 1; }

		public boolean contains(BlockPos p) {
			return p.getX() >= minX && p.getX() <= maxX
					&& p.getY() >= minY && p.getY() <= maxY
					&& p.getZ() >= minZ && p.getZ() <= maxZ;
		}

		public boolean onBoundary(BlockPos p) {
			return p.getX() == minX || p.getX() == maxX || p.getY() == minY || p.getY() == maxY
					|| p.getZ() == minZ || p.getZ() == maxZ;
		}
	}

	/** anchor の landmark から軸連結を辿って届く landmark 全ての包絡直方体を返す. */
	public static BlockVolume findVolume(Level level, BlockPos anchor) {
		Set<BlockPos> found = new HashSet<>();
		Deque<BlockPos> queue = new ArrayDeque<>();
		queue.add(anchor);
		while (!queue.isEmpty()) {
			BlockPos p = queue.poll();
			if (!found.add(p)) {
				continue;
			}
			for (Direction dir : Direction.values()) {
				BlockPos q = scanAxis(level, p, dir);
				if (q != null && !found.contains(q) && !((LandmarkBlock) level.getBlockState(q).getBlock()).isPath()) {
					queue.add(q);
				}
			}
		}
		found.removeIf(p -> !(level.getBlockState(p).getBlock() instanceof LandmarkBlock lb && !lb.isPath()));
		if (found.size() < 2) {
			return null;
		}
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPos p : found) {
			minX = Math.min(minX, p.getX()); maxX = Math.max(maxX, p.getX());
			minY = Math.min(minY, p.getY()); maxY = Math.max(maxY, p.getY());
			minZ = Math.min(minZ, p.getZ()); maxZ = Math.max(maxZ, p.getZ());
		}
		return new BlockVolume(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
