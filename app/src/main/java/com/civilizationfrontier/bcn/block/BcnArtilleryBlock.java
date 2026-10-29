package com.civilizationfrontier.bcn.block;

import com.civilizationfrontier.bcn.energy.BcnEfp;
import com.civilizationfrontier.bcn.entity.BcnEntities;
import com.civilizationfrontier.bcn.entity.BcnShellEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 重火器の発射装置基底.
 * 弾薬アイテムで装填(loaded)し、素手使用または RS 信号の立ち上がりで発射する.
 * 発射可否は BcnEntities.hasShellCapacity のエンティティ上限で制御される.
 */
public abstract class BcnArtilleryBlock extends Block {

	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);
	public static final BooleanProperty LOADED = BooleanProperty.create("loaded");
	public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");

	protected BcnArtilleryBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(LOADED, false)
				.setValue(TRIGGERED, false));
	}

	protected abstract Item ammoItem();

	protected abstract BcnShellEntity createShell(ServerLevel level, Vec3 muzzle);

	protected abstract Vec3 launchVector(Direction facing);

	protected abstract Vec3 muzzleOffset(Direction facing);

	protected abstract float velocity();

	protected abstract float inaccuracy();

	protected abstract long fireEfp();

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LOADED, TRIGGERED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return defaultBlockState()
				.setValue(FACING, ctx.getNearestLookingDirection().getOpposite())
				.setValue(TRIGGERED, ctx.getLevel().hasNeighborSignal(ctx.getClickedPos()));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!stack.is(ammoItem()) || state.getValue(LOADED)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			stack.consume(1, player);
			level.setBlock(pos, state.setValue(LOADED, true), UPDATE_ALL);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		if (!state.getValue(LOADED)) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel) {
			tryFire(serverLevel, pos, state);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			Orientation orientation, boolean movedByPiston) {
		boolean powered = level.hasNeighborSignal(pos);
		boolean triggered = state.getValue(TRIGGERED);
		if (powered == triggered) {
			return;
		}
		BlockState next = state.setValue(TRIGGERED, powered);
		level.setBlock(pos, next, UPDATE_ALL);
		if (powered && level instanceof ServerLevel serverLevel) {
			tryFire(serverLevel, pos, next);
		}
	}

	/** 装填済みかつ砲弾数が上限未満なら発射する. */
	protected boolean tryFire(ServerLevel level, BlockPos pos, BlockState state) {
		if (!state.getValue(LOADED) || !BcnEntities.hasShellCapacity(level)) {
			return false;
		}
		Direction facing = state.getValue(FACING);
		BcnShellEntity shell = createShell(level, Vec3.atCenterOf(pos).add(muzzleOffset(facing)));
		Vec3 direction = launchVector(facing);
		shell.shoot(direction.x, direction.y, direction.z, velocity(), inaccuracy());
		level.addFreshEntity(shell);
		level.setBlock(pos, state.setValue(LOADED, false), UPDATE_ALL);
		level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS,
				2.0F, 0.55F + level.getRandom().nextFloat() * 0.1F);
		BcnEfp.add(level.getServer(), fireEfp());
		return true;
	}
}
