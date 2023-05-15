package scanthosegates.console;

import scanthosegates.campaign.intel.CoronalHypershuntIntel;
import scanthosegates.campaign.intel.CryosleeperIntel;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

import java.util.ArrayList;

public class RemoveScanThoseGates implements BaseCommand {

    @Override
    public BaseCommand.CommandResult runCommand(String args, BaseCommand.CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }
        IntelManagerAPI intelManager = Global.getSector().getIntelManager();
        ArrayList<IntelInfoPlugin> all = new ArrayList<>();
        all.addAll(intelManager.getIntel(CoronalHypershuntIntel.class));
        all.addAll(intelManager.getIntel(CryosleeperIntel.class));
        Console.showMessage("Removing " + all.size() + " custom Scan Those Gates intel entries.");
        for (IntelInfoPlugin i : all) {
            intelManager.removeIntel(i);
        }
        CharacterDataAPI characterData = Global.getSector().getCharacterData();
        if (characterData.getAbilities().contains("stg_GateScanner")) {
            characterData.removeAbility("stg_GateScanner");
        }
        if (characterData.getAbilities().contains("stg_HypershuntScanner")){
            characterData.removeAbility("stg_HypershuntScanner");
        }
        if (characterData.getAbilities().contains("stg_CryosleeperScanner")){
            characterData.removeAbility("stg_CryosleeperScanner");
        }
        return BaseCommand.CommandResult.SUCCESS;
    }
}
