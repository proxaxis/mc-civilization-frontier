package com.civilizationfrontier.bcn.energy.block;

import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.energy.block.entity.ProcessorBlockEntity;
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
 * IC2 系加工機の共通ブロック. FACING = 正面 (加工表示), ACTIVE = 稼働表示.
 * アイテムを持って右クリックで入力スロットへ投入、素手右クリックで出力を回収する.
 */
public class MjProcessorBlock extends MjMachineBlock {

	private final String machine;

	public MjProcessorBlock(Properties properties, String machine) {
		super(properties);
		this.machine = machine;
	}

	public String machine() {
		return machine;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ProcessorBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return ticker(type, MachineBlockEntities.PROCESSOR, ProcessorBlockEntity::tick);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof ProcessorBlockEntity be)) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (!level.isClientSide()) {
			for (int slot = 0; slot < 2 && !stack.isEmpty(); slot++) {
				ItemStack cur = be.getItem(slot);
				if (cur.isEmpty()) {
					be.setItem(slot, stack.copyWithCount(1));
					stack.consume(1, player);
				} else if (ItemStack.isSameItemSameComponents(cur, stack) && cur.getCount() < cur.getMaxStackSize()) {
					cur.grow(1);
					stack.consume(1, player);
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof ProcessorBlockEntity be)) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			ItemStack out = be.getItem(2);
			if (!out.isEmpty()) {
				player.getInventory().add(out.copy());
				be.removeItemNoUpdate(2);
			}
		}
		return InteractionResult.SUCCESS;
	}
}
