package com.civilizationfrontier.bcn.energy.block;

import java.util.List;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.MjPressure;
import com.civilizationfrontier.bcn.energy.block.entity.CableSplitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 分割ケーブル. FACING = 指定入力面. 入力面から入ったパケットは
 * 他の利用可能レーンへラウンドロビンで振り分けられ、2 系統以上へ分流する.
 * 入力面以外から入ったパケットは通常ケーブルとして全方向へ流れる.
 */
public class MjCableSplitterBlock extends MjCableBlock {

	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);

	public MjCableSplitterBlock(Properties properties) {
		super(MjPressure.EV, 0.32, properties);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return super.getStateForPlacement(ctx).setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CableSplitterBlockEntity(pos, state);
	}

	@Override
	public List<Direction> routesFrom(Level level, BlockPos pos, Direction entry) {
		Direction input = level.getBlockState(pos).getValue(FACING);
		if (entry == null || entry == input) {
			if (level.getBlockEntity(pos) instanceof CableSplitterBlockEntity be) {
				Direction lane = be.nextLane(level, pos, input);
				return lane != null ? List.of(lane) : List.of();
			}
			return List.of();
		}
		return ALL_DIRECTIONS;
	}
}
