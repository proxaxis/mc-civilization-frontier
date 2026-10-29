package com.civilizationfrontier.bcn.lp;

/**
 * 物流パイプの機能種別.
 * CHASSIS 系はモジュールを挿して機能を持たせる受け皿で、moduleSlots が挿入可能数.
 */
public enum LpKind {
	BASIC(0),
	PROVIDER(0),
	REQUEST(0),
	SUPPLIER(0),
	CRAFTING(0),
	CHASSIS1(1),
	CHASSIS2(2),
	CHASSIS3(3),
	CHASSIS4(4),
	CHASSIS5(8);

	private final int moduleSlots;

	LpKind(int moduleSlots) {
		this.moduleSlots = moduleSlots;
	}

	public int moduleSlots() {
		return moduleSlots;
	}

	public boolean isChassis() {
		return moduleSlots > 0;
	}
}
