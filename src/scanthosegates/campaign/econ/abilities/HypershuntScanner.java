package scanthosegates.campaign.econ.abilities;

import scanthosegates.campaign.intel.CoronalHypershuntIntel;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class HypershuntScanner extends BaseDurationAbility {
    public static String CAN_SCAN_HYPERSHUNTS = "$HypershuntScannerAllowed";

    private static final Logger log = Global.getLogger(HypershuntScanner.class);
    static {log.setLevel(Level.ALL);}

    @Override
    protected void activateImpl() {

    }

    @Override
    protected void applyEffect(float amount, float level) {
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_HYPERSHUNTS)){
            for (SectorEntityToken hypershunt : Global.getSector().getCustomEntitiesWithTag(Tags.CORONAL_TAP)){
                if (tryCreateHypershuntReport(hypershunt, log, true, false)
                        && Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_HYPERSHUNTS)){
                    Global.getSector().getMemoryWithoutUpdate().set(CAN_SCAN_HYPERSHUNTS, false);
                }
            }
        }
    }

    @Override
    protected void deactivateImpl() {

    }

    @Override
    protected void cleanupImpl() {

    }

    @Override
    public boolean isUsable() {
        return Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_HYPERSHUNTS);
    }

    @Override
    public boolean hasTooltip(){return true;}

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        tooltip.addTitle("Remote Hypershunt Survey");
        float pad = 10f;
        tooltip.addPara("Remotely surveys all Coronal Hypershunts in the sector and adds them to the intel screen.", pad);

        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_HYPERSHUNTS)) {
            tooltip.addPara("Hypershunt survey is ready to activate.", Misc.getPositiveHighlightColor(), pad);
            tooltip.addPara("WARNING. Using this ability will reveal the basic layout of systems containing a hypershunt.", Misc.getHighlightColor(), pad);
        }
        else {
            tooltip.addPara("Hypershunt survey has already been used.", Misc.getNegativeHighlightColor(), pad);
        }
        addIncompatibleToTooltip(tooltip, expanded);
    }

    public static boolean tryCreateHypershuntReport(SectorEntityToken hypershunt, Logger log, boolean showMessage, boolean listener) {
        if ((!hypershunt.hasTag(Tags.CORONAL_TAP) || hypershunt.hasSensorProfile() || hypershunt.isDiscoverable()) && listener){
            return false;
        }

        IntelManagerAPI intelManager = Global.getSector().getIntelManager();
        for (IntelInfoPlugin intel : intelManager.getIntel(CoronalHypershuntIntel.class)) {
            CoronalHypershuntIntel hs = (CoronalHypershuntIntel) intel;
            if (hs.getEntity() == hypershunt) {
                return false; // report exists
            }
        }

        CoronalHypershuntIntel report = new CoronalHypershuntIntel(hypershunt);
        report.setNew(showMessage);
        intelManager.addIntel(report, !showMessage);
        log.info("Created intel report for hypershunt in " + hypershunt.getStarSystem());

        return true;
    }
}
