package com.civilizationfrontier.bcn.crop;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.item.BcnComponents;
import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 園芸用支柱. 耕地上に設置し作物を育てる. IC2 の Crop ブロック近似.
 *
 * - 空の支柱にもう 1 本支柱を使うと二重支柱 (cross) になり交差育種ベースになる.
 * - 種 (バニラ種 or 種袋) を使うと植え付け. 肥料/保水材/除草剤で 5 パラメータを調整.
 * - 素手右クリック: 成熟時は収穫、それ以外は簡易状態表示.
 */
public class CropStickBlock extends Block implements EntityBlock {

	public static final BooleanProperty CROSS = BooleanProperty.create("cross");
	public static final IntegerProperty GROWTH = IntegerProperty.create("growth", 0, 3);

	private static final VoxelShape SHAPE = Block.box(6.5, 0, 6.5, 9.5, 12, 9.5);

	public CropStickBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(CROSS, false)
				.setValue(GROWTH, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(CROSS, GROWTH);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CropStickBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return type == MachineBlockEntities.CROP_STICK
				? (l, p, s, be) -> { if (be instanceof CropStickBlockEntity ce) CropStickBlockEntity.tick(l, p, s, ce); }
				: null;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below()).getBlock() instanceof FarmlandBlock;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
			BlockPos pos, Direction dir, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (!canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
		return state;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof CropStickBlockEntity be)) {
			return InteractionResult.PASS;
		}

		// 支柱をもう 1 本: 交差育種ベースへ
		if (stack.is(BcnItems.CROP_STICK)) {
			if (state.getValue(CROSS) || be.hasCrop()) return InteractionResult.PASS;
			if (!level.isClientSide()) {
				level.setBlock(pos, state.setValue(CROSS, true), 3);
				stack.consume(1, player);
				level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.2F);
			}
			return InteractionResult.CONSUME;
		}

		// 作物分析機: 状態を鑑定表示
		if (stack.is(BcnItems.CROP_ANALYZER)) {
			sendAnalysis(player, be, state);
			return InteractionResult.SUCCESS;
		}

		// 種袋: 保持ステータスで植え付け
		if (stack.is(BcnItems.CROP_SEED_BAG)) {
			if (be.hasCrop()) return InteractionResult.PASS;
			BcnCropType type = BcnCropType.fromId(stack.getOrDefault(BcnComponents.CROP_ID, ""));
			if (type == null) return InteractionResult.PASS;
			be.plant(type,
					stack.getOrDefault(BcnComponents.CROP_GROWTH, 1),
					stack.getOrDefault(BcnComponents.CROP_GAIN, 1),
					stack.getOrDefault(BcnComponents.CROP_RESISTANCE, 1));
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
			return InteractionResult.CONSUME;
		}

		// 調整アイテム: パラメータ操作
		if (stack.is(BcnItems.FERTILIZER)) {
			be.addNutrients(50);
			stack.consume(1, player);
			sendStatus(player, be, "fertilizer");
			return InteractionResult.CONSUME;
		}
		if (stack.is(BcnItems.HYDRATED_COAL)) {
			be.addWater(50);
			stack.consume(1, player);
			sendStatus(player, be, "hydration");
			return InteractionResult.CONSUME;
		}
		if (stack.is(BcnItems.WEED_EX)) {
			be.addWeedEx(50);
			if (be.cropType() == BcnCropType.WEED) be.clearCrop();
			stack.consume(1, player);
			sendStatus(player, be, "weed-ex");
			return InteractionResult.CONSUME;
		}

		// バニラ種など: 既定ステータス (1/1/1) で植え付け
		BcnCropType seedType = BcnCropType.forSeed(stack.getItem());
		if (seedType != null && !state.getValue(CROSS)) {
			if (be.hasCrop()) return InteractionResult.PASS;
			be.plant(seedType, 1, 1, 1);
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
			return InteractionResult.CONSUME;
		}

		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof CropStickBlockEntity be)) {
			return InteractionResult.SUCCESS;
		}
		if (level instanceof ServerLevel server && be.harvest(server, pos, server.getRandom())) {
			level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 0.8F);
			return InteractionResult.SUCCESS;
		}
		sendAnalysis(player, be, state);
		return InteractionResult.SUCCESS;
	}

	private void sendStatus(Player player, CropStickBlockEntity be, String applied) {
		player.sendSystemMessage(Component.literal(String.format(
				"[CropStick] %s 適用: 栄養 %d, 保水 %d, 除草 %d",
				applied, be.nutrients(), be.waterStored(), be.weedExStored())));
	}

	/** 分析表示. 種名・成長・GGR ステータス・5 パラメータを列挙する. */
	public static void sendAnalysis(Player player, CropStickBlockEntity be, BlockState state) {
		Level level = player.level();
		BlockPos pos = be.getBlockPos();
		BcnCropType type = be.cropType();
		String cropLine = type == null
				? (state.getValue(CROSS) ? "交差育種ベース (空)" : "空の支柱")
				: String.format("%s (tier %d) stage %d/%d", type.id(), type.tier(), be.stage(), type.maxStage());
		player.sendSystemMessage(Component.literal("[Cropnalyzer] " + cropLine));
		if (type != null) {
			player.sendSystemMessage(Component.literal(String.format(
					"  GGR: growth %d, gain %d, resistance %d",
					be.growthStat(), be.gainStat(), be.resistanceStat())));
		}
		player.sendSystemMessage(Component.literal(String.format(
				"  光量 %d, 通気 %d, 栄養 %d, 保水 %d, 除草 %d",
				be.lightParam(level, pos), be.airParam(level, pos),
				be.nutritionParam(level, pos), be.hydrationParam(level, pos), be.herbicideParam())));
	}
}
