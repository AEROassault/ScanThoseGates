package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import data.campaign.econ.abilities.CryosleeperScanner;
import data.campaign.econ.abilities.GateScanner;
import data.campaign.econ.abilities.HypershuntScanner;

public class stg_ModPlugin extends BaseModPlugin {
    public static final String INTEL_MEGASTRUCTURES = "Megastructures";

    @Override
    public void onGameLoad(boolean newGame){
        MemoryAPI sectorMemory = Global.getSector().getMemoryWithoutUpdate();
        if (newGame){
            sectorMemory.set(GateScanner.UNSCANNED_GATES, true);
            sectorMemory.set(HypershuntScanner.CAN_SCAN_HYPERSHUNTS, true);
            sectorMemory.set(CryosleeperScanner.CAN_SCAN_CRYOSLEEPERS, true);
        }
    }
}
