package com.civilizationfrontier.bcn.block;

import java.util.List;

import com.civilizationfrontier.bcn.block.entity.MicroblockBlockEntity;
import com.civilizationfrontier.bcn.item.MicroblockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * マイクロブロック. 手ノコでブロックを分割した partial ブロック.
 * サイズは HALF (1/2, 8px) / PANEL (1/4, 4px) / COVER (1/8, 2px) の3段階で、
 * 素材ブロックは BlockEntity が保持する. 面に貼り付く (壁面カバー等も可).
 */
public class BcnMicroblockBlock extends FaceAttachedHorizontalDirectionalBlock implements EntityBlock {

	public enum MicroSize implements StringRepresentable {
		COVER(2), PANEL(4), HALF(8);

		private final int thickness;

		MicroSize(int thickness) {
			this.thickness = thickness;
		}

		/** 面からの厚み (px). */
		public int thickness() {
			return thickness;
		}

		/** 手ノコで次に分割できるサイズ. COVER は null (これ以上割れない). */
		public MicroSize next() {
			return switch (this) {
				case HALF -> PANEL;
				case PANEL -> COVER;
				case COVER -> null;
			};
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}
	}

	public static final EnumProperty<MicroSize> SIZE = EnumProperty.create("size", MicroSize.class);

	public BcnMicroblockBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(SIZE, MicroSize.HALF));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACE, FACING, SIZE);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MicroblockBlockEntity(pos, state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		int t = state.getValue(SIZE).thickness();
		return switch (state.getValue(FACE)) {
			case FLOOR -> Block.box(0, 0, 0, 16, t, 16);
			case CEILING -> Block.box(0, 16 - t, 0, 16, 16, 16);
			case WALL -> switch (state.getValue(FACING)) {
				case NORTH -> Block.box(0, 0, 0, 16, 16, t);
				case SOUTH -> Block.box(0, 0, 16 - t, 16, 16, 16);
				case WEST -> Block.box(0, 0, 0, t, 16, 16);
				case EAST -> Block.box(16 - t, 0, 0, 16, 16, 16);
				default -> Block.box(0, 0, 0, 16, t, 16);
			};
		};
	}

	public static ItemStack dropStack(BlockState state, BlockEntity be) {
		net.minecraft.world.level.block.Block material = net.minecraft.world.level.block.Blocks.STONE;
		if (be instanceof MicroblockBlockEntity mb) {
			material = mb.material();
		}
		return MicroblockItem.of(material, state.getValue(SIZE));
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
		BlockEntity be = params.getParameter(LootContextParams.BLOCK_ENTITY);
		return List.of(dropStack(state, be));
	}

	@Override
	protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state,
			boolean includeData) {
		return dropStack(state, level.getBlockEntity(pos));
	}
}
