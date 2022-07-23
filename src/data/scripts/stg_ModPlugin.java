package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateCMD;
import com.fs.starfarer.campaign.comms.v2.IntelManager;
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
        boolean revealAllGates = Global.getSettings().getBoolean("AddInactiveGatesToIntel");
        boolean scanAllGates = Global.getSettings().getBoolean("ScanAllGates");
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
                String gateStatusString = "null";
                boolean revealThatGate;
                for (SectorEntityToken gate : Global.getSector().getCustomEntitiesWithTag(Tags.GATE)) {
                    boolean gateScanStatus = gate.getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATE_SCANNED);
                    GateIntel gateIntelStatus = new GateIntel(gate);
                    revealThatGate = false;
                    try {
                        if ((systemsWithMarketList.contains(gate.getContainingLocation().getId()) && !gateScanStatus)
                                || (scanAllGates && !gateScanStatus)){
                            gate.getMemory().set(GateEntityPlugin.GATE_SCANNED, true);
                            GateCMD.notifyScanned(gate);
                            gateStatusString = " is activated.";
                            revealThatGate = true;
                        }
                        else {
                            gateStatusString = " is an inactive gate, ignoring.";
                        }
                    }
                    catch (Exception e) {
                        gateStatusString = " IS BROKEN. Exception: " + e;
                        revealThatGate = true;
                    }
                    finally {
                        try {
                            if (!Global.getSector().getIntelManager().hasIntel(gateIntelStatus) && revealAllGates) {
                                Global.getSector().getIntelManager().addIntel(gateIntelStatus);
                            } else if (!Global.getSector().getIntelManager().hasIntel(gateIntelStatus) && revealThatGate) {
                                Global.getSector().getIntelManager().addIntel(gateIntelStatus);
                            }
                        }
                        catch (Exception i) {
                            log.debug(gate.getName() + " in " + gate.getContainingLocation().getName() + " somehow broke the intel system. Exception: " + i);
                        }
                        log.debug(gate.getName() + " in " + gate.getContainingLocation().getName() + gateStatusString);
                    }
                }
            }
        }
    }
}