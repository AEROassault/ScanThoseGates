package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateCMD;
import exerelin.campaign.ExerelinSetupData;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class stg_ModPlugin extends BaseModPlugin {

    private static final Logger log = Global.getLogger(stg_ModPlugin.class);

    static {log.setLevel(Level.ALL);}

    public void onNewGameAfterTimePass() {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        boolean haveSkipStory = Global.getSettings().getModManager().isModEnabled("skipStory");
        if (haveNexerelin || haveSkipStory) {
            boolean activateThoseGates = false;
            if (haveNexerelin) { activateThoseGates = ExerelinSetupData.getInstance().skipStory; } //check for Nex before variable to avoid crash
            if (haveSkipStory) { activateThoseGates = true; } //otherwise, set true if Skip Story is enabled
            if (activateThoseGates) {
                for (SectorEntityToken gate : Global.getSector().getCustomEntitiesWithTag(Tags.GATE)) {
                    try {
                        String gateName = gate.getName();
                        if (!gateName.contains("Inactive") && !gate.getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATE_SCANNED)) {
                            gate.getMemory().set(GateEntityPlugin.GATE_SCANNED, true);
                            log.debug(gateName + " in system " + gate.getContainingLocation().getName() + " is activated.");
                            GateCMD.notifyScanned(gate);
                        }
                    } catch (Exception e) {log.debug(gate.getName() + " in system " + gate.getContainingLocation().getName() + " IS BROKEN.");}
                }
            }
        }
    }
}