package com.civilizationfrontier.bcn.builder;

import java.util.ArrayList;
import java.util.List;

import com.civilizationfrontier.bcn.builder.LandmarkBlock.BlockVolume;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

/**
 * 範囲保存アイテム. template=素材のみ (形状+素材種類), blueprint=完全保存.
 * map_location=座標記録, list=純アイテム基盤.
 */
public class SnapshotItem extends Item {

	public enum Kind { TEMPLATE, BLUEPRINT, MAP_LOCATION, LIST }

	private final Kind kind;

	public SnapshotItem(Properties properties, Kind kind) {
		super(properties.stacksTo(1));
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	// ---- スナップショットデータ ----

	public static CompoundTag capture(Level level, BlockVolume vol, boolean full) {
		CompoundTag tag = new CompoundTag();
		tag.putIntArray("size", new int[]{vol.sizeX(), vol.sizeY(), vol.sizeZ()});
		List<String> palette = new ArrayList<>();
		ListTag cells = new ListTag();
		for (int y = vol.minY(); y <= vol.maxY(); y++) {
			for (int z = vol.minZ(); z <= vol.maxZ(); z++) {
				for (int x = vol.minX(); x <= vol.maxX(); x++) {
					BlockState s = level.getBlockState(new BlockPos(x, y, z));
					String name = s.isAir() ? "minecraft:air"
							: BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString();
					if (!full && s.isAir()) {
						name = "minecraft:air";
					}
					int idx = palette.indexOf(name);
					if (idx < 0) {
						palette.add(name);
						idx = palette.size() - 1;
					}
					cells.add(net.minecraft.nbt.IntTag.valueOf(idx));
				}
			}
		}
		ListTag pal = new ListTag();
		for (String p : palette) {
			pal.add(net.minecraft.nbt.StringTag.valueOf(p));
		}
		tag.put("palette", pal);
		tag.put("cells", cells);
		return tag;
	}

	public static CompoundTag read(ItemStack stack) {
		CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
		return cd == null ? null : cd.copyTag();
	}

	public static void write(ItemStack stack, CompoundTag data) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
	}

	public static Block decode(String name) {
		return BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.parse(name));
	}

	public List<String> palette(ItemStack stack) {
		CompoundTag tag = read(stack);
		List<String> out = new ArrayList<>();
		if (tag == null) {
			return out;
		}
		for (net.minecraft.nbt.Tag t : tag.getListOrEmpty("palette")) {
			out.add(t.asString().orElse(""));
		}
		return out;
	}

	public int[] size(ItemStack stack) {
		CompoundTag tag = read(stack);
		return tag == null ? null : tag.getIntArray("size").orElse(null);
	}

	public ListTag cells(ItemStack stack) {
		CompoundTag tag = read(stack);
		return tag == null ? null : tag.getListOrEmpty("cells");
	}

	// ---- map_location: 右クリックブロックで座標を記録 ----

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		if (kind != Kind.MAP_LOCATION || ctx.getLevel().isClientSide()) {
			return super.useOn(ctx);
		}
		ItemStack stack = ctx.getItemInHand();
		CompoundTag tag = read(stack) != null ? read(stack) : new CompoundTag();
		ListTag marks = tag.getListOrEmpty("marks");
		BlockPos p = ctx.getClickedPos();
		CompoundTag m = new CompoundTag();
		m.putIntArray("pos", new int[]{p.getX(), p.getY(), p.getZ()});
		marks.add(m);
		tag.put("marks", marks);
		write(stack, tag);
		if (ctx.getPlayer() != null) {
			ctx.getPlayer().sendSystemMessage(Component.literal("Marked " + p.getX() + "," + p.getY() + "," + p.getZ()));
		}
		return InteractionResult.SUCCESS;
	}

	public static List<BlockPos> marks(ItemStack stack) {
		List<BlockPos> out = new ArrayList<>();
		CompoundTag tag = read(stack);
		if (tag != null) {
			for (net.minecraft.nbt.Tag t : tag.getListOrEmpty("marks")) {
				t.asCompound().flatMap(c -> c.getIntArray("pos")).ifPresent(a -> {
					if (a.length == 3) {
						out.add(new BlockPos(a[0], a[1], a[2]));
					}
				});
			}
		}
		return out;
	}
}
