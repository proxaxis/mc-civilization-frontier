package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.block.entity.GeothermalBlockEntity;
import com.civilizationfrontier.bcn.energy.block.entity.ReactorBlockEntity;
import com.civilizationfrontier.bcn.energy.block.entity.SolarGeneratorBlockEntity;
import com.civilizationfrontier.bcn.energy.block.entity.WaterGeneratorBlockEntity;
import com.civilizationfrontier.bcn.energy.block.entity.WindGeneratorBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * IC2 系発電機ブロック. FACING = MJ 出力面、ACTIVE = 発電中表示.
 * 燃料/環境の判定は BlockEntity 側の自動判定のみ (UI なし).
 */
public class Ic2GeneratorBlock extends MjMachineBlock {

	public enum Gen {
		GEOTHERMAL, WATER, WIND, SOLAR, REACTOR
	}

	private final Gen gen;

	public Ic2GeneratorBlock(Gen gen, Properties properties) {
		super(properties);
		this.gen = gen;
	}

	public Gen gen() {
		return gen;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return switch (gen) {
			case GEOTHERMAL -> new GeothermalBlockEntity(pos, state);
			case WATER -> new WaterGeneratorBlockEntity(pos, state);
			case WIND -> new WindGeneratorBlockEntity(pos, state);
			case SOLAR -> new SolarGeneratorBlockEntity(pos, state);
			case REACTOR -> new ReactorBlockEntity(pos, state);
		};
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return switch (gen) {
			case GEOTHERMAL -> ticker(type, MachineBlockEntities.GEOTHERMAL_GENERATOR,
					GeothermalBlockEntity::tick);
			case WATER -> ticker(type, MachineBlockEntities.WATER_GENERATOR,
					WaterGeneratorBlockEntity::tick);
			case WIND -> ticker(type, MachineBlockEntities.WIND_GENERATOR,
					WindGeneratorBlockEntity::tick);
			case SOLAR -> ticker(type, MachineBlockEntities.SOLAR_GENERATOR,
					SolarGeneratorBlockEntity::tick);
			case REACTOR -> ticker(type, MachineBlockEntities.REACTOR, ReactorBlockEntity::tick);
		};
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof GeothermalBlockEntity be
				&& FluidStorageUtil.interactWithFluidStorage(be.fluidStorage(hit.getDirection()), player, hand)) {
			return InteractionResult.SUCCESS;
		}
		if (level.getBlockEntity(pos) instanceof ReactorBlockEntity be && !stack.isEmpty()) {
			if (!level.isClientSide() && be.insertFuel(stack)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.CONSUME;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
}
