package com.civilizationfrontier.bcn.block.entity;

import java.util.ArrayList;
import java.util.List;

import com.civilizationfrontier.bcn.block.BcnGateBlock;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * 集積回路ゲート (IC gate) の BlockEntity. 内部に最大 {@value #MAX_CELLS} 個の
 * 論理セルを記憶し、それらを1ブロック分の入出力として評価する.
 *
 * セルは (type, inMask, outMask, src) を持つ:
 * - inMask/outMask はローカル端子 (front=1, back=2, left=4, right=8) のビット
 * - src >= 0 のとき、セルの全入力は番号の小さい先行セルの出力ビットに置き換わる
 *   (セル連鎖で多段論理を構成できる)
 */
public class IcGateBlockEntity extends BlockEntity {

	public static final int MAX_CELLS = 4;

	public static final int SIDE_FRONT = 1;
	public static final int SIDE_BACK = 2;
	public static final int SIDE_LEFT = 4;
	public static final int SIDE_RIGHT = 8;

	public record Cell(String type, int inMask, int outMask, int src) {
		public static final Codec<Cell> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(Codec.STRING.fieldOf("t").forGetter(Cell::type),
						Codec.INT.optionalFieldOf("in", 0).forGetter(Cell::inMask),
						Codec.INT.optionalFieldOf("out", 0).forGetter(Cell::outMask),
						Codec.INT.optionalFieldOf("src", -1).forGetter(Cell::src))
				.apply(instance, Cell::new));
	}

	private final List<Cell> cells = new ArrayList<>();
	private final int[] mem = new int[MAX_CELLS];

	public IcGateBlockEntity(BlockPos pos, BlockState state) {
		super(MachineBlockEntities.IC_GATE, pos, state);
	}

	public List<Cell> cells() {
		return cells;
	}

	public boolean addCell(BcnGateBlock.GateType type) {
		if (cells.size() >= MAX_CELLS) {
			return false;
		}
		cells.add(new Cell(type.name().toLowerCase(), defaultInMask(type), defaultOutMask(type), -1));
		setChanged();
		return true;
	}

	public Cell removeLastCell() {
		if (cells.isEmpty()) {
			return null;
		}
		Cell removed = cells.remove(cells.size() - 1);
		setChanged();
		return removed;
	}

	/** ゲート種別のデフォルト入力マスク ({@link BcnGateBlock} の端子レイアウトに準拠). */
	public static int defaultInMask(BcnGateBlock.GateType type) {
		return switch (type) {
			case NOT, BUFFER -> SIDE_BACK;
			case OR, NOR, AND, NAND -> SIDE_LEFT | SIDE_RIGHT | SIDE_BACK;
			case XOR, XNOR, RS_LATCH, T_LATCH -> SIDE_LEFT | SIDE_RIGHT;
		};
	}

	/** ゲート種別のデフォルト出力マスク. */
	public static int defaultOutMask(BcnGateBlock.GateType type) {
		return switch (type) {
			case NOT, BUFFER -> SIDE_FRONT | SIDE_LEFT | SIDE_RIGHT;
			case OR, NOR, AND, NAND, XOR, XNOR -> SIDE_FRONT;
			case RS_LATCH, T_LATCH -> SIDE_FRONT | SIDE_BACK;
		};
	}

	/**
	 * ブロックのローカル側入力 (4bit) を受けて、駆動すべきローカル側のビットを返す.
	 * セルは記憶順に評価され、src 参照は先行セルのみ有効.
	 */
	public int evaluate(int in) {
		int outs = 0;
		for (int i = 0; i < cells.size(); i++) {
			Cell c = cells.get(i);
			boolean srcBit = false;
			if (c.src() >= 0 && c.src() < i) {
				srcBit = lastOut[c.src()] != 0;
			}
			int eff = c.src() >= 0 ? (srcBit ? c.inMask() : 0) : (in & c.inMask());
			int[] memRef = {mem[i]};
			boolean bit = truth(c.type(), eff, c.inMask(), memRef);
			mem[i] = memRef[0];
			lastOut[i] = bit ? 1 : 0;
			if (bit) {
				outs |= c.outMask();
			}
		}
		return outs;
	}

	private final int[] lastOut = new int[MAX_CELLS];

	private static boolean truth(String type, int in, int inMask, int[] mem) {
		return switch (type) {
			case "not" -> in == 0;
			case "buffer" -> in != 0;
			case "or" -> in != 0;
			case "nor" -> in == 0;
			case "and" -> inMask != 0 && in == inMask;
			case "nand" -> inMask == 0 || in != inMask;
			case "xor" -> Integer.bitCount(in) % 2 == 1;
			case "xnor" -> Integer.bitCount(in) % 2 == 0;
			case "rs_latch" -> {
				boolean set = (in & lowestBit(inMask)) != 0;
				boolean reset = (in & (inMask & ~lowestBit(inMask))) != 0;
				if (set && reset) {
					mem[0] = 0;
				} else if (set) {
					mem[0] = 1;
				} else if (reset) {
					mem[0] = 0;
				}
				yield mem[0] != 0;
			}
			case "t_latch" -> {
				boolean inAny = in != 0;
				boolean prev = (mem[0] & 2) != 0;
				int state = mem[0] & 1;
				if (inAny && !prev) {
					state ^= 1;
				}
				mem[0] = state | (inAny ? 2 : 0);
				yield state != 0;
			}
			default -> false;
		};
	}

	private static int lowestBit(int mask) {
		return mask & -mask;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("cells", Codec.list(Cell.CODEC), List.copyOf(cells));
		output.putIntArray("cell_mem", mem);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		input.read("cells", Codec.list(Cell.CODEC)).ifPresent(list -> {
			cells.clear();
			cells.addAll(list);
		});
		int[] loaded = input.getIntArray("cell_mem").orElse(new int[0]);
		System.arraycopy(loaded, 0, mem, 0, Math.min(loaded.length, mem.length));
	}
}
