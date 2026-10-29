package com.civilizationfrontier.bcn.fluid;

import java.util.Optional;

import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.civilizationfrontier.bcn.item.BcnItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/** 金オイル (精製燃料). リファイナリー産出物で、黒オイルより低粘性で流れやすい. */
public abstract class FuelFluid extends FlowingFluid {
	@Override
	public Fluid getFlowing() {
		return BcnFluids.FLOWING_FUEL;
	}

	@Override
	public Fluid getSource() {
		return BcnFluids.FUEL;
	}

	@Override
	public Item getBucket() {
		return BcnItems.FUEL_BUCKET;
	}

	@Override
	protected boolean canConvertToSource(ServerLevel level) {
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
		Block.dropResources(state, level, pos, blockEntity);
	}

	@Override
	public int getSlopeFindDistance(LevelReader level) {
		return 4;
	}

	@Override
	public int getDropOff(LevelReader level) {
		return 3;
	}

	@Override
	public int getTickDelay(LevelReader level) {
		return 8;
	}

	@Override
	public BlockState createLegacyBlock(FluidState state) {
		return BcnBlocks.FUEL.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
	}

	@Override
	public boolean isSame(Fluid fluid) {
		return fluid == BcnFluids.FUEL || fluid == BcnFluids.FLOWING_FUEL;
	}

	@Override
	public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
		return false;
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BUCKET_FILL);
	}

	public static class Flowing extends FuelFluid {
		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}
	}

	public static class Still extends FuelFluid {
		@Override
		public int getAmount(FluidState state) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}
	}
}
