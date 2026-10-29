package com.civilizationfrontier.bcn.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.civilizationfrontier.bcn.block.BcnTankBlock;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 液体タンク. 1段あたり 16 バケツ. 縦積みの連結タンクを1つのカラムとして扱う. */
public class TankBlockEntity extends BlockEntity {

	public static final long CAPACITY = FluidConstants.BUCKET * 16;

	private FluidVariant variant = FluidVariant.blank();
	private long amount;

	public TankBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.TANK, pos, state);
	}

	/** このタンクを含む連結カラム (下から順). */
	private List<TankBlockEntity> column() {
		List<TankBlockEntity> list = new ArrayList<>();
		BlockPos.MutableBlockPos cursor = worldPosition.mutable();
		while (level != null && level.getBlockEntity(cursor.move(Direction.DOWN)) instanceof TankBlockEntity) {
			cursor.move(Direction.DOWN);
		}
		for (BlockPos p = cursor.immutable(); ; p = p.above()) {
			if (level.getBlockEntity(p) instanceof TankBlockEntity t) {
				list.add(t);
			} else {
				break;
			}
		}
		return list;
	}

	private boolean accepts(FluidVariant v) {
		return variant.isBlank() || variant.equals(v);
	}

	public Storage<FluidVariant> fluidStorage(Direction face) {
		TankBlockEntity self = this;
		return new Storage<>() {
			@Override
			public long insert(FluidVariant resource, long maxAmount, TransactionContext tx) {
				if (resource == null || resource.isBlank() || maxAmount <= 0) {
					return 0;
				}
				long accepted = 0;
				for (TankBlockEntity t : self.column()) {
					if (!t.accepts(resource)) {
						continue;
					}
					long room = CAPACITY - t.amount;
					long take = Math.min(room, maxAmount - accepted);
					if (take > 0) {
						tx.addOuterCloseCallback(r -> {
							if (r.wasCommitted()) {
								t.variant = resource;
								t.amount += take;
								t.setChanged();
							}
						});
						accepted += take;
					}
					if (accepted >= maxAmount) {
						break;
					}
				}
				return accepted;
			}

			@Override
			public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
				if (resource == null || resource.isBlank() || maxAmount <= 0) {
					return 0;
				}
				long extracted = 0;
				List<TankBlockEntity> col = self.column();
				for (int i = col.size() - 1; i >= 0 && extracted < maxAmount; i--) {
					TankBlockEntity t = col.get(i);
					if (t.amount <= 0 || !t.variant.equals(resource)) {
						continue;
					}
					long take = Math.min(t.amount, maxAmount - extracted);
					tx.addOuterCloseCallback(r -> {
						if (r.wasCommitted()) {
							t.amount -= take;
							if (t.amount <= 0) {
								t.variant = FluidVariant.blank();
							}
							t.setChanged();
						}
					});
					extracted += take;
				}
				return extracted;
			}

			@Override
			public Iterator<StorageView<FluidVariant>> iterator() {
				List<StorageView<FluidVariant>> views = new ArrayList<>();
				for (TankBlockEntity t : self.column()) {
					views.add(new StorageView<>() {
						@Override public long extract(FluidVariant r, long max, TransactionContext tx) { return 0; }
						@Override public boolean isResourceBlank() { return t.amount <= 0; }
						@Override public FluidVariant getResource() { return t.variant; }
						@Override public long getAmount() { return t.amount; }
						@Override public long getCapacity() { return CAPACITY; }
					});
				}
				return views.iterator();
			}
		};
	}

	@Override
	protected void saveAdditional(ValueOutput out) {
		super.saveAdditional(out);
		out.putString("fl", variant.isBlank() ? "" : BuiltInRegistries.FLUID.getKey(variant.getFluid()).toString());
		out.putLong("amt", amount);
	}

	@Override
	protected void loadAdditional(ValueInput in) {
		super.loadAdditional(in);
		String id = in.getStringOr("fl", "");
		Identifier fid = id.isEmpty() ? null : Identifier.tryParse(id);
		Fluid fluid = fid == null ? null : BuiltInRegistries.FLUID.getValue(fid);
		variant = fluid == null ? FluidVariant.blank() : FluidVariant.of(fluid);
		amount = fluid == null ? 0 : in.getLongOr("amt", 0);
	}
}
