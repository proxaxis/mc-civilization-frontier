package com.civilizationfrontier.bcn.railway;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Block section (閉塞) signal. A train is held on the guard rail while another
 * train occupies the section ahead. Trains leaving the section pass the signal
 * freely, since only carts moving in the entry direction are held.
 */
public class RailSignalBlockEntity extends BlockEntity {
	private static final int RESCAN_INTERVAL = 20;
	private static final double ENTER_SPEED = 0.005;

	private TrackScanner.Section section;
	private List<BlockPos> inward = List.of();
	private boolean dirty = true;
	private int rescanCooldown = 0;
	private final Map<UUID, HeldCart> held = new LinkedHashMap<>();
	private final Set<UUID> committed = new LinkedHashSet<>();

	public RailSignalBlockEntity(BlockPos pos, BlockState state) {
		super(BcnRailway.RAIL_SIGNAL_BE, pos, state);
	}

	public RailSignalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public void markDirty() {
		dirty = true;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, RailSignalBlockEntity be) {
		be.serverTick(level, pos, state);
	}

	private void serverTick(Level level, BlockPos pos, BlockState state) {
		if (--rescanCooldown <= 0 || dirty) {
			rescanCooldown = RESCAN_INTERVAL;
			rescan(level, pos, state);
		}

		boolean occupied = section != null && !section.rails().isEmpty() && occupied(level);
		SignalAspect aspect = section == null || section.rails().isEmpty() || occupied
				? SignalAspect.STOP
				: SignalAspect.CLEAR;

		if (aspect == SignalAspect.STOP) {
			applyStops(level);
		} else {
			releaseHeld(level);
		}
		holdTick(level);

		if (state.getValue(RailSignalBlock.ASPECT) != aspect) {
			level.setBlock(pos, state.setValue(RailSignalBlock.ASPECT, aspect), 3);
		}
	}

	private void rescan(Level level, BlockPos pos, BlockState state) {
		dirty = false;
		Direction facing = state.getValue(RailSignalBlock.FACING);
		BlockPos guardRail = TrackScanner.findGuardRail(level, pos, facing);
		if (guardRail == null) {
			section = null;
			inward = List.of();
			return;
		}
		section = TrackScanner.scan(level, guardRail, facing);
		inward = TrackScanner.inwardConnections(level, section);
	}

	private boolean occupied(Level level) {
		AABB bounds = section.bounds();
		List<AbstractMinecart> carts = level.getEntities(
				EntityTypeTest.forClass(AbstractMinecart.class), bounds,
				cart -> !held.containsKey(cart.getUUID()) && onSection(cart));
		return !carts.isEmpty();
	}

	private boolean onSection(AbstractMinecart cart) {
		BlockPos p = cart.blockPosition();
		return section.contains(p) || section.contains(p.above()) || section.contains(p.below());
	}

	private void applyStops(Level level) {
		if (section == null || section.rails().isEmpty()) {
			return;
		}
		BlockPos guardRail = section.guardRail();
		AABB around = new AABB(guardRail).inflate(0.5);
		for (AbstractMinecart cart : level.getEntities(EntityTypeTest.forClass(AbstractMinecart.class),
				around, cart -> true)) {
			if (!cart.blockPosition().equals(guardRail) || held.containsKey(cart.getUUID())) {
				continue;
			}
			if (committed.contains(cart.getUUID())) {
				continue;
			}
			if (!ImBridge.isHead(cart) || !entering(cart)) {
				continue;
			}
			Vec3 push = cart instanceof MinecartFurnace furnace ? furnace.push : Vec3.ZERO;
			held.put(cart.getUUID(),
					new HeldCart(cart.getUUID(), cart.getDeltaMovement(), push, cart.position()));
			setChanged();
		}
	}

	/** A cart on the guard rail is entering when it moves toward a section rail. */
	private boolean entering(AbstractMinecart cart) {
		Vec3 v = cart.getDeltaMovement();
		BlockPos g = section.guardRail();
		if (inward.isEmpty()) {
			return v.horizontalDistanceSqr() > ENTER_SPEED * ENTER_SPEED;
		}
		for (BlockPos c : inward) {
			double dot = v.x * (c.getX() - g.getX()) + v.z * (c.getZ() - g.getZ());
			if (dot > ENTER_SPEED) {
				return true;
			}
		}
		return false;
	}

	private void holdTick(Level level) {
		for (HeldCart h : held.values()) {
			Entity entity = level.getEntity(h.id());
			if (!(entity instanceof AbstractMinecart cart) || !cart.isAlive()) {
				continue;
			}
			cart.setPos(h.pos().x, h.pos().y, h.pos().z);
			cart.setDeltaMovement(Vec3.ZERO);
			if (cart instanceof MinecartFurnace furnace) {
				furnace.push = Vec3.ZERO;
			}
		}
	}

	private void releaseHeld(Level level) {
		for (HeldCart h : held.values()) {
			Entity entity = level.getEntity(h.id());
			if (!(entity instanceof AbstractMinecart cart)) {
				continue;
			}
			cart.setDeltaMovement(h.motion());
			if (cart instanceof MinecartFurnace furnace) {
				furnace.push = h.push();
			}
			committed.add(h.id());
		}
		if (!held.isEmpty()) {
			held.clear();
			setChanged();
		}
		committed.removeIf(id -> {
			Entity entity = level.getEntity(id);
			return !(entity instanceof AbstractMinecart cart) || !cart.isAlive()
					|| !cart.blockPosition().equals(section.guardRail());
		});
	}

	public List<String> debugInfo() {
		List<String> info = new ArrayList<>();
		if (section == null) {
			info.add("no guard rail found");
		} else {
			info.add("guard_rail=" + section.guardRail().toShortString()
					+ " section_rails=" + section.rails().size()
					+ " held=" + held.size());
		}
		return info;
	}

	private record HeldCart(UUID id, Vec3 motion, Vec3 push, Vec3 pos) {
		private static final Codec<HeldCart> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("id").forGetter(HeldCart::id),
						Vec3.CODEC.fieldOf("motion").forGetter(HeldCart::motion),
						Vec3.CODEC.fieldOf("push").forGetter(HeldCart::push),
						Vec3.CODEC.fieldOf("pos").forGetter(HeldCart::pos))
				.apply(instance, HeldCart::new));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("held", Codec.list(HeldCart.CODEC), List.copyOf(held.values()));
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		input.read("held", Codec.list(HeldCart.CODEC)).ifPresent(list -> {
			held.clear();
			for (HeldCart h : list) {
				held.put(h.id(), h);
			}
		});
	}
}
