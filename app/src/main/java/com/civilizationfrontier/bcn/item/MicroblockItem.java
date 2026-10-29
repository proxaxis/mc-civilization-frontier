package com.civilizationfrontier.bcn.item;

import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.civilizationfrontier.bcn.block.BcnMicroblockBlock;
import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;

/**
 * マイクロブロックアイテム. 素材ブロックと分割サイズをコンポーネントで保持する.
 * サイズは {@link DataComponents#BLOCK_STATE}、素材は {@link DataComponents#BLOCK_ENTITY_DATA}
 * に載せ、通常の BlockItem 設置経路でブロックに反映される.
 */
public class MicroblockItem extends BlockItem {

	public MicroblockItem(Block block, Properties properties) {
		super(block, properties);
	}

	public static ItemStack of(Block material, BcnMicroblockBlock.MicroSize size) {
		ItemStack stack = new ItemStack(BcnItems.MICROBLOCK);
		stack.set(DataComponents.BLOCK_STATE,
				BlockItemStateProperties.EMPTY.with(BcnMicroblockBlock.SIZE, size));
		CompoundTag tag = new CompoundTag();
		tag.putString("material", BuiltInRegistries.BLOCK.getKey(material).toString());
		stack.set(DataComponents.BLOCK_ENTITY_DATA,
				TypedEntityData.of(MachineBlockEntities.MICROBLOCK, tag));
		return stack;
	}
}
