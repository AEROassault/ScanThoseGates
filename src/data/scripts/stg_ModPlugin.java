package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import data.campaign.econ.abilities.CryosleeperScanner;
import data.campaign.econ.abilities.GateScanner;
import data.campaign.econ.abilities.HypershuntScanner;

public class stg_ModPlugin extends BaseModPlugin {
    boolean prepForRemoval = Global.getSettings().getBoolean("PrepareForRemoval");
    boolean captainsLogActive = Global.getSettings().getModManager().isModEnabled("CaptainsLog");

    @Override
    public void onGameLoad(boolean newGame){
        MemoryAPI sectorMemory = Global.getSector().getMemoryWithoutUpdate();
        CharacterDataAPI characterData = Global.getSector().getCharacterData();

        if (newGame){
            sectorMemory.set(GateScanner.UNSCANNED_GATES, true);
        }
        if (captainsLogActive) {
            if (!sectorMemory.contains(HypershuntScanner.CAN_SCAN_HYPERSHUNTS) && !prepForRemoval && !characterData.getAbilities().contains("stg_HypershuntScanner")) {
                characterData.addAbility("stg_HypershuntScanner");
                sectorMemory.set(HypershuntScanner.CAN_SCAN_HYPERSHUNTS, true);
            }
            if (!sectorMemory.contains(CryosleeperScanner.CAN_SCAN_CRYOSLEEPERS) && !prepForRemoval && !characterData.getAbilities().contains("stg_CryosleeperScanner")) {
                characterData.addAbility("stg_CryosleeperScanner");
                sectorMemory.set(CryosleeperScanner.CAN_SCAN_CRYOSLEEPERS, true);
            }
        }
        else {
            if (characterData.getAbilities().contains("stg_HypershuntScanner")){
                characterData.removeAbility("stg_HypershuntScanner");
            }
            if (characterData.getAbilities().contains("stg_CryosleeperScanner")){
                characterData.removeAbility("stg_CryosleeperScanner");
            }
        }

        if (prepForRemoval) {
            if (characterData.getAbilities().contains("stg_GateScanner")) {
                characterData.removeAbility("stg_GateScanner");
            }
            if (characterData.getAbilities().contains("stg_HypershuntScanner")){
                characterData.removeAbility("stg_HypershuntScanner");
            }
            if (characterData.getAbilities().contains("stg_CryosleeperScanner")){
                characterData.removeAbility("stg_CryosleeperScanner");
            }
        }
    }
}
