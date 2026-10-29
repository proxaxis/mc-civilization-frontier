package com.civilizationfrontier.bcn.block.entity;

import com.civilizationfrontier.bcn.Bcn;
import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.civilizationfrontier.bcn.energy.block.entity.BatteryBlockEntity;
import com.civilizationfrontier.bcn.energy.block.entity.ConsumerBlockEntity;
import com.civilizationfrontier.bcn.energy.block.entity.GeneratorBlockEntity;
import com.civilizationfrontier.bcn.energy.block.entity.TransformerBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class MachineBlockEntities {

	public static final BlockEntityType<PlanterBlockEntity> PLANTER = register("planter",
			FabricBlockEntityTypeBuilder.create(PlanterBlockEntity::new, BcnBlocks.PLANTER).build());
	public static final BlockEntityType<MjSourceBlockEntity> MJ_SOURCE = register("mj_source",
			FabricBlockEntityTypeBuilder.create(MjSourceBlockEntity::new, BcnBlocks.MJ_SOURCE).build());
	public static final BlockEntityType<ItemPipeBlockEntity> ITEM_PIPE = register("item_pipe",
			FabricBlockEntityTypeBuilder.create(ItemPipeBlockEntity::new, BcnBlocks.ITEM_PIPE).build());
	public static final BlockEntityType<FluidPipeBlockEntity> WATERPROOF_PIPE = register("waterproof_pipe",
			FabricBlockEntityTypeBuilder.create(FluidPipeBlockEntity::new, BcnBlocks.WATERPROOF_PIPE).build());
	public static final BlockEntityType<GeneratorBlockEntity> GENERATOR = register("mj_generator",
			FabricBlockEntityTypeBuilder.create(GeneratorBlockEntity::new, BcnBlocks.MJ_GENERATOR).build());
	public static final BlockEntityType<ConsumerBlockEntity> CONSUMER = register("mj_consumer",
			FabricBlockEntityTypeBuilder.create(ConsumerBlockEntity::new, BcnBlocks.MJ_CONSUMER).build());
	public static final BlockEntityType<TransformerBlockEntity> TRANSFORMER = register("transformer",
			FabricBlockEntityTypeBuilder.create(TransformerBlockEntity::new,
					BcnBlocks.TRANSFORMER_LV, BcnBlocks.TRANSFORMER_MV, BcnBlocks.TRANSFORMER_HV).build());
	public static final BlockEntityType<BatteryBlockEntity> BATTERY = register("battery",
			FabricBlockEntityTypeBuilder.create(BatteryBlockEntity::new,
					BcnBlocks.BATTERY_BOX, BcnBlocks.MFE).build());
	public static final BlockEntityType<EngineBlockEntity> ENGINE = register("engine",
			FabricBlockEntityTypeBuilder.create(EngineBlockEntity::new,
					BcnBlocks.ENGINE_STONE, BcnBlocks.ENGINE_COMBUSTION).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.QuarryBlockEntity> QUARRY = register("quarry",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.energy.block.entity.QuarryBlockEntity::new,
					BcnBlocks.QUARRY).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.RefineryBlockEntity> REFINERY = register("refinery",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.energy.block.entity.RefineryBlockEntity::new,
					BcnBlocks.REFINERY).build());

	private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Bcn.id(name), type);
	}

	public static void init() {
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.storage(face), WATERPROOF_PIPE);
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.fluidStorage(face), ENGINE);
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.fluidStorage(face), REFINERY);
	}
}
