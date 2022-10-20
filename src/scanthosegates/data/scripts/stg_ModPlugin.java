package scanthosegates.data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import scanthosegates.data.campaign.econ.abilities.CryosleeperScanner;
import scanthosegates.data.campaign.econ.abilities.GateScanner;
import scanthosegates.data.campaign.econ.abilities.HypershuntScanner;
import scanthosegates.data.campaign.listeners.RelocationListener;
import scanthosegates.data.campaign.listeners.SalvagingListener;

public class stg_ModPlugin extends BaseModPlugin {
    public static final String INTEL_MEGASTRUCTURES = "Megastructures";

    @Override
    public void onGameLoad(boolean newGame){
        MemoryAPI sectorMemory = Global.getSector().getMemoryWithoutUpdate();
        CharacterDataAPI characterData = Global.getSector().getCharacterData();

        if (!sectorMemory.contains(GateScanner.UNSCANNED_GATES)) {
            sectorMemory.set(GateScanner.UNSCANNED_GATES, true);
        }
        if (!sectorMemory.contains(HypershuntScanner.CAN_SCAN_HYPERSHUNTS)) {
            sectorMemory.set(HypershuntScanner.CAN_SCAN_HYPERSHUNTS, true);
        }
        if (!sectorMemory.contains(CryosleeperScanner.CAN_SCAN_CRYOSLEEPERS)) {
            sectorMemory.set(CryosleeperScanner.CAN_SCAN_CRYOSLEEPERS, true);
        }

        if (!characterData.getAbilities().contains("stg_GateScanner")) {
            characterData.addAbility("stg_GateScanner");
        }
        if (!characterData.getAbilities().contains("stg_HypershuntScanner")) {
            characterData.addAbility("stg_HypershuntScanner");
        }
        if (!characterData.getAbilities().contains("stg_CryosleeperScanner")) {
            characterData.addAbility("stg_CryosleeperScanner");
        }

        Global.getSector().getListenerManager().addListener(new RelocationListener(), true);
        Global.getSector().getListenerManager().addListener(new SalvagingListener(), true);
    }
}
