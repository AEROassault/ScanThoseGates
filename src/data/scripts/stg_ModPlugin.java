package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateCMD;
import exerelin.campaign.ExerelinSetupData;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class stg_ModPlugin extends BaseModPlugin {

    List<String> systemsWithMarketList = new ArrayList<>();
    private static final Logger log = Global.getLogger(stg_ModPlugin.class);

    static {log.setLevel(Level.ALL);}

    public void onNewGameAfterTimePass() {
        boolean haveNexerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        boolean haveSkipStory = Global.getSettings().getModManager().isModEnabled("skipStory");
        if (haveNexerelin || haveSkipStory) {
            boolean activateThoseGates;

            if (haveNexerelin) { activateThoseGates = ExerelinSetupData.getInstance().skipStory; }
            else { activateThoseGates = true; }

            if (activateThoseGates) {
                for (LocationAPI systemWithMarket : Global.getSector().getEconomy().getStarSystemsWithMarkets()) {
                    for (MarketAPI market : Global.getSector().getEconomy().getMarkets(systemWithMarket)){
                        if (!market.isHidden()) {
                            systemsWithMarketList.add(systemWithMarket.getId());
                            break;
                        }
                    }
                }
                for (SectorEntityToken gate : Global.getSector().getCustomEntitiesWithTag(Tags.GATE)) {
                    try {
                        if (systemsWithMarketList.contains(gate.getContainingLocation().getId())
                                && !gate.getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATE_SCANNED)) {
                            gate.getMemory().set(GateEntityPlugin.GATE_SCANNED, true);
                            GateCMD.notifyScanned(gate);
                            log.debug(gate.getName() + " in system " + gate.getContainingLocation().getName() + " is activated.");
                        }
                    } catch (Exception e) {
                        log.debug(gate.getName() + " in system " + gate.getContainingLocation().getName() + " IS BROKEN. Exception: " + e);
                    }
                }
            }
        }
    }
}