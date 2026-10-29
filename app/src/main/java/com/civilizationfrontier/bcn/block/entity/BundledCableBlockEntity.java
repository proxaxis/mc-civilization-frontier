package com.civilizationfrontier.bcn.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * ケーブル束の BlockEntity. 16 チャネル分の powered をビットマスクで保持する.
 * 値は {@link com.civilizationfrontier.bcn.block.ColoredWireNetwork} が書き込む.
 */
public class BundledCableBlockEntity extends BlockEntity {

	private int mask;

	public BundledCableBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.BUNDLED_CABLE, pos, state);
	}

	public int mask() {
		return mask;
	}

	public boolean channel(int color) {
		return (mask & (1 << color)) != 0;
	}

	public void setChannel(int color, boolean on) {
		int bit = 1 << color;
		int next = on ? mask | bit : mask & ~bit;
		if (next != mask) {
			mask = next;
			setChanged();
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("channels", mask);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		mask = input.getIntOr("channels", 0);
	}
}
