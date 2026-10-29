package com.civilizationfrontier.bcn;

import com.civilizationfrontier.bcn.block.BcnBlocks;
import com.civilizationfrontier.bcn.block.entity.MachineBlockEntities;
import com.civilizationfrontier.bcn.entity.BcnEntities;
import com.civilizationfrontier.bcn.fluid.BcnFluids;
import com.civilizationfrontier.bcn.item.BcnItemGroup;
import com.civilizationfrontier.bcn.item.BcnItems;
import com.civilizationfrontier.bcn.worldgen.BcnFeatures;
import com.civilizationfrontier.bcn.economy.BcnCastTest;
import com.civilizationfrontier.bcn.economy.BcnEconomy;
import com.civilizationfrontier.bcn.railway.BcnRailway;
import com.civilizationfrontier.bcn.worldgen.BcnWorldgen;
import net.fabricmc.api.ModInitializer;

public class BuildCraftNeo implements ModInitializer {
	public static final String MOD_ID = "bcn";

	@Override
	public void onInitialize() {
		BcnFluids.init();
		BcnBlocks.init();
		MachineBlockEntities.init();
		BcnItems.init();
		BcnEntities.init();
		BcnItemGroup.init();
		BcnFeatures.init();
		BcnWorldgen.init();
		BcnEconomy.init();
		BcnCastTest.init();
		BcnRailway.init();
		BcnCommands.init();
	}
}
