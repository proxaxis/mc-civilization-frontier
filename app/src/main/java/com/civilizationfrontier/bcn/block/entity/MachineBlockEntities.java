package com.civilizationfrontier.bcn.block.entity;

import com.civilizationfrontier.bcn.Bcn;
import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.civilizationfrontier.bcn.crop.BoozeBarrelBlockEntity;
import com.civilizationfrontier.bcn.crop.CropStickBlockEntity;
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
					BcnBlocks.BATTERY_BOX, BcnBlocks.BATBOX, BcnBlocks.MFE, BcnBlocks.MFSU,
					BcnBlocks.BT_BATTERY_BOX).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.CableDetectorBlockEntity> CABLE_DETECTOR = register("cable_detector",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.energy.block.entity.CableDetectorBlockEntity::new,
					BcnBlocks.CABLE_DETECTOR).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.CableSplitterBlockEntity> CABLE_SPLITTER = register("cable_splitter",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.energy.block.entity.CableSplitterBlockEntity::new,
					BcnBlocks.CABLE_SPLITTER).build());
	public static final BlockEntityType<EngineBlockEntity> ENGINE = register("engine",
			FabricBlockEntityTypeBuilder.create(EngineBlockEntity::new,
					BcnBlocks.ENGINE_STONE, BcnBlocks.ENGINE_COMBUSTION, BcnBlocks.ENGINE_REDSTONE, BcnBlocks.ENGINE_CREATIVE).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.builder.FillerBlock.FillerBlockEntity> FILLER = register("filler",
			FabricBlockEntityTypeBuilder.create(com.civilizationfrontier.bcn.builder.FillerBlock.FillerBlockEntity::new,
					BcnBlocks.FILLER).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.builder.BuilderBlock.BuilderBlockEntity> BUILDER = register("builder",
			FabricBlockEntityTypeBuilder.create(com.civilizationfrontier.bcn.builder.BuilderBlock.BuilderBlockEntity::new,
					BcnBlocks.BUILDER).build());
	public static final BlockEntityType<AssemblyTableBlockEntity> ASSEMBLY_TABLE = register("assembly_table",
			FabricBlockEntityTypeBuilder.create(AssemblyTableBlockEntity::new, BcnBlocks.ASSEMBLY_TABLE).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.LaserBlockEntity> LASER = register("laser",
			FabricBlockEntityTypeBuilder.create(com.civilizationfrontier.bcn.energy.block.entity.LaserBlockEntity::new,
					BcnBlocks.LASER).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.QuarryBlockEntity> QUARRY = register("quarry",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.energy.block.entity.QuarryBlockEntity::new,
					BcnBlocks.QUARRY).build());
		public static final BlockEntityType<TankBlockEntity> TANK = register("tank",
			FabricBlockEntityTypeBuilder.create(TankBlockEntity::new, BcnBlocks.TANK).build());
	public static final BlockEntityType<PumpBlockEntity> PUMP = register("pump",
			FabricBlockEntityTypeBuilder.create(PumpBlockEntity::new, BcnBlocks.PUMP).build());
	public static final BlockEntityType<ChuteBlockEntity> CHUTE = register("chute",
			FabricBlockEntityTypeBuilder.create(ChuteBlockEntity::new, BcnBlocks.CHUTE).build());
	public static final BlockEntityType<MiningWellBlockEntity> MINING_WELL = register("mining_well",
			FabricBlockEntityTypeBuilder.create(MiningWellBlockEntity::new, BcnBlocks.MINING_WELL).build());
	public static final BlockEntityType<AutoWorkbenchBlockEntity> AUTO_WORKBENCH = register("auto_workbench",
			FabricBlockEntityTypeBuilder.create(AutoWorkbenchBlockEntity::new, BcnBlocks.AUTO_WORKBENCH).build());
	public static final BlockEntityType<FilteredBufferBlockEntity> FILTERED_BUFFER = register("filtered_buffer",
			FabricBlockEntityTypeBuilder.create(FilteredBufferBlockEntity::new, BcnBlocks.FILTERED_BUFFER).build());
	public static final BlockEntityType<FloodGateBlockEntity> FLOOD_GATE = register("flood_gate",
			FabricBlockEntityTypeBuilder.create(FloodGateBlockEntity::new, BcnBlocks.FLOOD_GATE).build());
	public static final BlockEntityType<CropStickBlockEntity> CROP_STICK = register("crop_stick",
			FabricBlockEntityTypeBuilder.create(CropStickBlockEntity::new, BcnBlocks.CROP_STICK).build());
	public static final BlockEntityType<BoozeBarrelBlockEntity> BOOZE_BARREL = register("booze_barrel",
			FabricBlockEntityTypeBuilder.create(BoozeBarrelBlockEntity::new, BcnBlocks.BOOZE_BARREL).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.RefineryBlockEntity> REFINERY = register("refinery",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.energy.block.entity.RefineryBlockEntity::new,
					BcnBlocks.REFINERY).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.lp.LogisticsPipeBlockEntity> LOGISTICS_PIPE = register(
			"logistics_pipe",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.lp.LogisticsPipeBlockEntity::new,
					BcnBlocks.PIPE_BASIC_LOGISTICS, BcnBlocks.PIPE_PROVIDER, BcnBlocks.PIPE_REQUEST,
					BcnBlocks.PIPE_SUPPLIER, BcnBlocks.PIPE_CRAFTING, BcnBlocks.PIPE_CHASSIS_MK1,
					BcnBlocks.PIPE_CHASSIS_MK2, BcnBlocks.PIPE_CHASSIS_MK3, BcnBlocks.PIPE_CHASSIS_MK4,
					BcnBlocks.PIPE_CHASSIS_MK5).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.lp.LiquidSupplierPipeBlockEntity> LIQUID_SUPPLIER = register(
			"pipe_liquid_supplier",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.lp.LiquidSupplierPipeBlockEntity::new,
					BcnBlocks.PIPE_LIQUID_SUPPLIER).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.lp.SolderingStationBlockEntity> SOLDERING_STATION = register(
			"soldering_station",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.lp.SolderingStationBlockEntity::new,
					BcnBlocks.SOLDERING_STATION).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.ProcessorBlockEntity> PROCESSOR = register("processor",
			FabricBlockEntityTypeBuilder.create(
					com.civilizationfrontier.bcn.energy.block.entity.ProcessorBlockEntity::new,
					BcnBlocks.MACERATOR, BcnBlocks.EXTRACTOR, BcnBlocks.COMPRESSOR,
					BcnBlocks.CANNER, BcnBlocks.ALLOY_SMELTER).build());

	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.GeothermalBlockEntity> GEOTHERMAL_GENERATOR = register("generator_geothermal",
			FabricBlockEntityTypeBuilder.create(com.civilizationfrontier.bcn.energy.block.entity.GeothermalBlockEntity::new,
					BcnBlocks.GENERATOR_GEOTHERMAL).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.WaterGeneratorBlockEntity> WATER_GENERATOR = register("generator_water",
			FabricBlockEntityTypeBuilder.create(com.civilizationfrontier.bcn.energy.block.entity.WaterGeneratorBlockEntity::new,
					BcnBlocks.GENERATOR_WATER).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.WindGeneratorBlockEntity> WIND_GENERATOR = register("generator_wind",
			FabricBlockEntityTypeBuilder.create(com.civilizationfrontier.bcn.energy.block.entity.WindGeneratorBlockEntity::new,
					BcnBlocks.GENERATOR_WIND).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.SolarGeneratorBlockEntity> SOLAR_GENERATOR = register("generator_solar",
			FabricBlockEntityTypeBuilder.create(com.civilizationfrontier.bcn.energy.block.entity.SolarGeneratorBlockEntity::new,
					BcnBlocks.GENERATOR_SOLAR, BcnBlocks.SOLAR_PANEL).build());
	public static final BlockEntityType<BundledCableBlockEntity> BUNDLED_CABLE = register("bundled_cable",
			FabricBlockEntityTypeBuilder.create(BundledCableBlockEntity::new, BcnBlocks.BUNDLED_CABLE).build());
	public static final BlockEntityType<IcGateBlockEntity> IC_GATE = register("ic_gate",
			FabricBlockEntityTypeBuilder.create(IcGateBlockEntity::new, BcnBlocks.IC_GATE).build());
	public static final BlockEntityType<MicroblockBlockEntity> MICROBLOCK = register("microblock",
			FabricBlockEntityTypeBuilder.create(MicroblockBlockEntity::new, BcnBlocks.MICROBLOCK).build());
	public static final BlockEntityType<com.civilizationfrontier.bcn.energy.block.entity.ReactorBlockEntity> REACTOR = register("reactor",
			FabricBlockEntityTypeBuilder.create(com.civilizationfrontier.bcn.energy.block.entity.ReactorBlockEntity::new,
					BcnBlocks.REACTOR).build());

	private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> type) {
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Bcn.id(name), type);
	}

	public static void init() {
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.storage(face), WATERPROOF_PIPE);
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.fluidStorage(face), ENGINE);
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.fluidStorage(face), REFINERY);
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.fluidStorage(face), TANK);
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.fluidStorage(face), PUMP);
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.fluidStorage(face), FLOOD_GATE);
		FluidStorage.SIDED.registerForBlockEntity((be, face) -> be.fluidStorage(face), GEOTHERMAL_GENERATOR);
	}
}
