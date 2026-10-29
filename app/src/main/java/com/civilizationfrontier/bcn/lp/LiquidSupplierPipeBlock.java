package com.civilizationfrontier.bcn.lp;

import com.civilizationfrontier.bcn.block.BcnPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 液体補給パイプ. 防水パイプ網に接続し、隣接タンクへ指定液体を
 * 網内の供給タンクから配送させ続ける (補給パイプの液体版).
 */
public class LiquidSupplierPipeBlock extends BcnPipeBlock {

	public LiquidSupplierPipeBlock(Properties properties) {
		super(properties, true);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LiquidSupplierPipeBlockEntity(pos, state);
	}
}
