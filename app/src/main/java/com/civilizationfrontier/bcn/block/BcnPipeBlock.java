package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.block.entity.FluidPipeBlockEntity;
import com.civilizationfrontier.bcn.block.entity.ItemPipeBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BcnPipeBlock extends Block implements EntityBlock {

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

	public final boolean fluid;

	public BcnPipeBlock(Properties properties, boolean fluid) {
		super(properties);
		this.fluid = fluid;
		registerDefaultState(stateDefinition.any()
				.setValue(NORTH, Conn.NONE).setValue(SOUTH, Conn.NONE)
				.setValue(EAST, Conn.NONE).setValue(WEST, Conn.NONE)
				.setValue(UP, Conn.NONE).setValue(DOWN, Conn.NONE));
	}

	public static EnumProperty<Conn> property(Direction dir) {
		return switch (dir) {
		case NORTH -> NORTH;
		case SOUTH -> SOUTH;
		case EAST -> EAST;
		case WEST -> WEST;
		case UP -> UP;
		case DOWN -> DOWN;
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return fluid ? new FluidPipeBlockEntity(pos, state) : new ItemPipeBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return (lvl, pos, st, be) -> {
			if (be instanceof ItemPipeBlockEntity item) {
				item.tick(lvl, pos);
			} else if (be instanceof FluidPipeBlockEntity fluid) {
				fluid.tick(lvl, pos);
			}
		};
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
		if (neighbor.getBlock() instanceof BcnPipeBlock other) {
			// 同種パイプのみ接続 (アイテム管とケーブル等は混線しない)
			return other.fluid == fluid && other.getClass() == getClass() ? Conn.PIPE : Conn.NONE;
		}
		if (neighbor.hasBlockEntity()) return Conn.BLOCK; // 容器・機械のみ終端接続
		return Conn.NONE;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (fluid && level.getBlockEntity(pos) instanceof FluidPipeBlockEntity be
				&& FluidStorageUtil.interactWithFluidStorage(be.storage(hit.getDirection()), player, hand)) {
			return InteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
}
