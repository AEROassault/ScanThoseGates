package scanthosegates.campaign.econ.abilities;

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

import java.util.HashSet;

import static java.lang.Math.pow;
import static scanthosegates.ScannerModPlugin.ActivateAllGates;
import static scanthosegates.ScannerModPlugin.RevealAllGates;

public class GateScanner extends BaseDurationAbility {
    public static String UNSCANNED_GATES = "$UnscannedGatesFound";
    HashSet<LocationAPI> systemsWithMarkets = new HashSet<>();
    private static final Logger log = Global.getLogger(GateScanner.class);
    static {log.setLevel(Level.ALL);}
    boolean gateScanPrimed;
    final float checksReportInterval = 60;
    float secondsSinceLastReport = 0;
    final float secondsBetweenChecks = 1;
    float secondsSinceLastCheck = 0;

    @Override
    protected void activateImpl() {}

    @Override
    protected void applyEffect(float amount, float level) {
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(UNSCANNED_GATES)) {
            long startTime = System.nanoTime();
            systemsWithMarkets = generateMarketSystemsHashset();
            String gateStatusString = "null";
            for (SectorEntityToken gate : Global.getSector().getCustomEntitiesWithTag(Tags.GATE)) {
                boolean gateScanStatus = gate.getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATE_SCANNED);
                boolean revealThatGate = false;
                try {
                    if ((systemsWithMarkets.contains(gate.getContainingLocation()) && !gateScanStatus)
                            || (ActivateAllGates && !gateScanStatus)) {
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
                        if (gateIntelDoesNotExist(gate)) {
                            if (RevealAllGates) {
                                Global.getSector().getIntelManager().addIntel(new GateIntel(gate));
                            } else if (revealThatGate) {
                                Global.getSector().getIntelManager().addIntel(new GateIntel(gate));
                            }
                        }
                    } catch (Exception CannotAddGateIntel) {
                        log.debug(gate.getName() + " in " + gate.getContainingLocation().getName() + " somehow broke the intel system. Exception: " + CannotAddGateIntel);
                    }
                    log.info(gate.getName() + " in " + gate.getContainingLocation().getName() + gateStatusString);
                }
            }
            Global.getSector().getMemoryWithoutUpdate().set(UNSCANNED_GATES, false);
            systemsWithMarkets.clear();
            long elapsedTime = System.nanoTime() - startTime;
            log.info("It took " + elapsedTime / pow(10, 9) + " seconds (" + elapsedTime / pow(10, 6) + " milliseconds or "
                    + elapsedTime + " nanoseconds) to execute the gate scan.");

        }
    }

    @Override
    protected void deactivateImpl() {}

    @Override
    protected void cleanupImpl() {}

    @Override
    public void advance(float amount) {
        super.advance(amount);
        secondsSinceLastCheck += amount;
        secondsSinceLastReport += amount;
        if (secondsSinceLastCheck > secondsBetweenChecks) {
            long startUsableCheck = System.nanoTime();
            checkForGates();
            secondsSinceLastCheck = 0;
            systemsWithMarkets.clear();
            long endUsableCheck = System.nanoTime() - startUsableCheck;
            if (secondsSinceLastReport > checksReportInterval) {
                log.info("CheckForGates() took " + endUsableCheck / pow(10, 9) + " seconds (" + endUsableCheck/pow(10, 6)
                        + " milliseconds or " + endUsableCheck + " nanoseconds) to complete.");
                secondsSinceLastReport = 0;
            }
        }
    }

    @Override
    public boolean isUsable() {
        return Global.getSector().getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.CAN_SCAN_GATES)
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATES_ACTIVE)
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(UNSCANNED_GATES);
    }

    @Override
    public boolean hasTooltip() {return true;}

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        gateScanPrimed = Global.getSector().getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.CAN_SCAN_GATES)
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATES_ACTIVE);
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        tooltip.addTitle("Remote Gate Scan");
        float pad = 10f;

        if (RevealAllGates && !ActivateAllGates) {
            tooltip.addPara("Scans all gates located in systems with at least one non-hidden market " +
                    "and adds all gates to the intel screen, regardless of market presence in the gate's system.", pad);
        }
        else if (ActivateAllGates) {
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

    public boolean gateIntelDoesNotExist(SectorEntityToken gate) {
        for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(GateIntel.class)){
            GateIntel gi = (GateIntel) intel;
            if (gi.getGate() == gate) {
                return false;
            }
        }
        return true;
    }

    public HashSet<LocationAPI> generateMarketSystemsHashset() {
        for (LocationAPI systemWithMarket : Global.getSector().getEconomy().getStarSystemsWithMarkets()) {
            if (!systemsWithMarkets.contains(systemWithMarket)) {
                for (MarketAPI market : Global.getSector().getEconomy().getMarkets(systemWithMarket)) {
                    if (!market.isHidden()) {
                        systemsWithMarkets.add(systemWithMarket);
                        break;
                    }
                }
            }
        }
        return systemsWithMarkets;
    }

    public void checkForGates() {
        systemsWithMarkets = generateMarketSystemsHashset();
        for (SectorEntityToken gate : Global.getSector().getCustomEntitiesWithTag(Tags.GATE)) {
            if (!gate.getMemoryWithoutUpdate().getBoolean(GateEntityPlugin.GATE_SCANNED)) {
                if (ActivateAllGates) {
                    Global.getSector().getMemoryWithoutUpdate().set(UNSCANNED_GATES, true);
                    return;
                } else if (RevealAllGates && gateIntelDoesNotExist(gate)) {
                    Global.getSector().getMemoryWithoutUpdate().set(UNSCANNED_GATES, true);
                    return;
                } else if (systemsWithMarkets.contains(gate.getContainingLocation())) {
                    Global.getSector().getMemoryWithoutUpdate().set(UNSCANNED_GATES, true);
                    return;
                }
            }
        }
    }
}
