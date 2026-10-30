package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.block.entity.AssemblyTableBlockEntity;
import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
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
 * 精密作業台. 素材の右クリック投入と、素手右クリックでのエネルギー表示のみ.
 * MJ 出力面は持たず、動力はレーザー照射のみ (FACING/ACTIVE は表示用).
 */
public class MjAssemblyTableBlock extends MjMachineBlock {

	public MjAssemblyTableBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AssemblyTableBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return ticker(type, MachineBlockEntities.ASSEMBLY_TABLE, AssemblyTableBlockEntity::tick);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.getBlockEntity(pos) instanceof AssemblyTableBlockEntity be && !stack.isEmpty()) {
			if (!level.isClientSide()) {
				for (int i = 0; i < be.items().size(); i++) {
					ItemStack slot = be.items().get(i);
					if (slot.isEmpty()) {
						be.items().set(i, stack.copyWithCount(1));
						stack.shrink(1);
						be.setChanged();
						return InteractionResult.SUCCESS;
					} else if (ItemStack.isSameItemSameComponents(slot, stack)
							&& slot.getCount() < slot.getMaxStackSize()) {
						slot.grow(1);
						stack.shrink(1);
						be.setChanged();
						return InteractionResult.SUCCESS;
					}
				}
			}
			return InteractionResult.CONSUME;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}
}
