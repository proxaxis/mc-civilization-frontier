package com.civilizationfrontier.bcn.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * マイクロブロックの素材保持 BlockEntity.
 * 素材はレジストリ名 (例 "minecraft:stone") で保存し、復元時は存在しない
 * ブロックなら stone にフォールバックする.
 */
public class MicroblockBlockEntity extends BlockEntity {

	private Block material = Blocks.STONE;

	public MicroblockBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.MICROBLOCK, pos, state);
	}

	public Block material() {
		return material;
	}

	public void setMaterial(Block material) {
		this.material = material;
		setChanged();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("material", BuiltInRegistries.BLOCK.getKey(material).toString());
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		material = input.getString("material")
				.map(name -> BuiltInRegistries.BLOCK.getValue(Identifier.parse(name)))
				.orElse(Blocks.STONE);
	}
}
