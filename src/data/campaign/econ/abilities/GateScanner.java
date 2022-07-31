package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateCMD;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;

public class GateScanner extends BaseDurationAbility {

    boolean revealAllGates = Global.getSettings().getBoolean("AddInactiveGatesToIntel");
    boolean scanAllGates = Global.getSettings().getBoolean("ScanAllGates");
    List<String> systemsWithMarketList = new ArrayList<>();
    private static final Logger log = Global.getLogger(GateScanner.class);
    static {log.setLevel(Level.ALL);}
    protected Boolean primed = null;

    @Override
    protected void activateImpl() {
    }

    @Override
    protected void applyEffect(float amount, float level) {
        scanThoseGates();
    }

    @Override
    protected void deactivateImpl() {

    }

    @Override
    protected void cleanupImpl() {

    }

    @Override
    public boolean isUsable() {
        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        primed = mem.getBoolean(GateEntityPlugin.CAN_SCAN_GATES) && mem.getBoolean(GateEntityPlugin.GATES_ACTIVE);
        return primed;
    }

    @Override
    public boolean hasTooltip(){return true;}

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        LabelAPI title = tooltip.addTitle("Remote Gate Scan");
        float pad = 10f;

        if (revealAllGates && !scanAllGates) {
            tooltip.addPara("Scans all gates located in systems with at least one non-hidden market " +
                    "and adds all gates to the intel screen, regardless of market presence in the gate's system.", pad);
        }
        else if (scanAllGates) {
            tooltip.addPara("Scans all gates regardless of market presence in the gate's system.", pad);
        }
        else {
            tooltip.addPara("Scans all gates located in systems with at least one non-hidden market " +
                    "and adds them to the intel screen.", 10f);
        }
        if (!primed) {
            tooltip.addPara("Cannot activate gates yet, the Janus Device has not been acquired.", Misc.getNegativeHighlightColor(), pad);
        }
        else {
            tooltip.addPara("The Janus Device has been acquired, gates can now be scanned.", Misc.getPositiveHighlightColor(), pad);
        }
        addIncompatibleToTooltip(tooltip, expanded);
    }
    public void scanThoseGates() {
        long startTime = System.nanoTime();
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
                    gateStatusString = " is in a marketless system, ignoring.";
                }
            }
            catch (Exception CannotScanGate) {
                gateStatusString = " IS BROKEN. Exception: " + CannotScanGate;
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
                catch (Exception CannotAddGateIntel) {
                    log.debug(gate.getName() + " in " + gate.getContainingLocation().getName() + " somehow broke the intel system. Exception: " + CannotAddGateIntel);
                }
                log.debug(gate.getName() + " in " + gate.getContainingLocation().getName() + gateStatusString);
            }
        }
        long endTime = System.nanoTime();
        long elapsedTime = (endTime - startTime);
        log.debug("Scan Those Gates took " + elapsedTime/pow(10, 9) + " seconds (" + elapsedTime + " nanoseconds) to execute.");
    }
}
