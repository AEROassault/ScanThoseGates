package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;

public class GateScanner extends BaseDurationAbility {
    public static String UNSCANNED_GATES = "$UnscannedGatesFound";
    public static boolean revealAllGates = Global.getSettings().getBoolean("AddInactiveGatesToIntel");
    public static boolean scanAllGates = Global.getSettings().getBoolean("ScanAllGates");
    List<LocationAPI> systemsWithMarketList = new ArrayList<>();
    List<SectorEntityToken> unscannedGates = new ArrayList<>();
    private static final Logger log = Global.getLogger(data.campaign.econ.abilities.GateScanner.class);
    static {log.setLevel(Level.ALL);}
    boolean gateScanPrimed;

    @Override
    protected void activateImpl() {

    }

    @Override
    protected void applyEffect(float amount, float level) {
        long startTime = System.nanoTime();
        for (LocationAPI systemWithMarket : Global.getSector().getEconomy().getStarSystemsWithMarkets()) {
            for (MarketAPI market : Global.getSector().getEconomy().getMarkets(systemWithMarket)) {
                if (!market.isHidden() && !systemsWithMarketList.contains(systemWithMarket)) {
                    systemsWithMarketList.add(systemWithMarket);
                    break;
                }
            }
        }
        String gateStatusString = "null";
        for (SectorEntityToken gate : Global.getSector().getCustomEntitiesWithTag(Tags.GATE)) {
            boolean gateScanStatus = gate.getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATE_SCANNED);
            boolean revealThatGate = false;
            try {
                if ((systemsWithMarketList.contains(gate.getContainingLocation()) && !gateScanStatus)
                        || (scanAllGates && !gateScanStatus)) {
                    gate.getMemory().set(GateEntityPlugin.GATE_SCANNED, true);
                    GateCMD.notifyScanned(gate);
                    gateStatusString = " is activated.";
                    revealThatGate = true;
                } else if (gateScanStatus) {
                    gateStatusString = " is already scanned, ignoring.";
                } else {
                    gateStatusString = " is in a marketless system, ignoring.";
                }
            } catch (Exception CannotScanGate) {
                gateStatusString = " IS BROKEN. Exception: " + CannotScanGate;
                revealThatGate = true;
            } finally {
                try {
                    if (!doesGateIntelExist(gate)) {
                        if (revealAllGates) {
                            Global.getSector().getIntelManager().addIntel(new GateIntel(gate));
                        } else if (revealThatGate) {
                            Global.getSector().getIntelManager().addIntel(new GateIntel(gate));
                        }
                    }
                } catch (Exception CannotAddGateIntel) {
                    log.debug(gate.getName() + " in " + gate.getContainingLocation().getName() + " somehow broke the intel system. Exception: " + CannotAddGateIntel);
                }
                log.debug(gate.getName() + " in " + gate.getContainingLocation().getName() + gateStatusString);
            }
        }
        systemsWithMarketList.clear();
        unscannedGates.clear();
        Global.getSector().getMemoryWithoutUpdate().set(UNSCANNED_GATES, false);
        long elapsedTime = System.nanoTime() - startTime;
        log.debug("It took " + elapsedTime / pow(10, 9) + " seconds (" + elapsedTime + " nanoseconds) to execute the gate scan.");
    }

    @Override
    protected void deactivateImpl() {

    }

    @Override
    protected void cleanupImpl() {

    }

    @Override
    public boolean isUsable() {
//        long startUseableCheck = System.nanoTime();
        checkForGates();
//        long endUseableCheck = System.nanoTime() - startUseableCheck;
//        log.debug("isUsable() took " + endUseableCheck/pow(10, 9) + " seconds (" + endUseableCheck + " nanoseconds) to check.");
        return Global.getSector().getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.CAN_SCAN_GATES)
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATES_ACTIVE)
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(UNSCANNED_GATES);
    }

    @Override
    public boolean hasTooltip(){return true;}

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        gateScanPrimed = Global.getSector().getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.CAN_SCAN_GATES)
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATES_ACTIVE);
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        tooltip.addTitle("Remote Gate Scan");
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
        if (!gateScanPrimed) {
            tooltip.addPara("Cannot activate gates yet, the Janus Device has not been acquired.",
                    Misc.getNegativeHighlightColor(), pad);
        }
        else if (!Global.getSector().getMemoryWithoutUpdate().getBoolean(UNSCANNED_GATES)) {
            tooltip.addPara("The Janus Device has been acquired, but there are no gates available to scan.",
                    Misc.getNegativeHighlightColor(), pad);
        }
        else {
            tooltip.addPara("The Janus Device has been acquired and there are gates available to scan.",
                    Misc.getPositiveHighlightColor(), pad);
        }
        addIncompatibleToTooltip(tooltip, expanded);
    }

    public boolean doesGateIntelExist(SectorEntityToken gate) {
        for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(GateIntel.class)){
            GateIntel gi = (GateIntel) intel;
            if (gi.getGate() == gate) {
                return true;
            }
        }
        return false;
    }

    public void checkForGates(){
        for (LocationAPI systemWithMarket : Global.getSector().getEconomy().getStarSystemsWithMarkets()) {
            if (!systemsWithMarketList.contains(systemWithMarket)) {
                for (MarketAPI market : Global.getSector().getEconomy().getMarkets(systemWithMarket)) {
                    if (!market.isHidden()) {
                        systemsWithMarketList.add(systemWithMarket);
                        break;
                    }
                }
            }
        }
        if (unscannedGates.isEmpty()){
            for (SectorEntityToken gate : Global.getSector().getCustomEntitiesWithTag(Tags.GATE)) {
                if (!gate.getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATE_SCANNED) && !unscannedGates.contains(gate)) {
                    unscannedGates.add(gate);
                }
            }
        }
        for (SectorEntityToken gate : unscannedGates) {
            if (scanAllGates) {
                Global.getSector().getMemoryWithoutUpdate().set(UNSCANNED_GATES, true);
            } else if (systemsWithMarketList.contains(gate.getContainingLocation())) {
                Global.getSector().getMemoryWithoutUpdate().set(UNSCANNED_GATES, true);
            }
        }
    }
}
