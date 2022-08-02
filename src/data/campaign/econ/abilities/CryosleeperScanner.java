package data.campaign.econ.abilities;

import CaptainsLog.campaign.intel.UnremovableIntel;
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

public class CryosleeperScanner extends BaseDurationAbility {
    public static String CAN_SCAN_CRYOSLEEPERS = "$CryosleeperScannerAllowed";
    private static final Logger log = Global.getLogger(data.campaign.econ.abilities.CryosleeperScanner.class);
    static {log.setLevel(Level.ALL);}

    @Override
    protected void activateImpl() {

    }

    @Override
    protected void applyEffect(float amount, float level) {
        startCryosleeperScan();
    }

    @Override
    protected void deactivateImpl() {

    }

    @Override
    protected void cleanupImpl() {

    }

    @Override
    public boolean isUsable() {
        return Global.getSettings().getModManager().isModEnabled("CaptainsLog")
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_CRYOSLEEPERS);
    }

    @Override
    public boolean hasTooltip(){return true;}

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        tooltip.addTitle("Remote Cryosleeper Survey");
        float pad = 10f;
        tooltip.addPara("Remotely surveys all Cryosleepers in the sector and adds them to the intel screen.",
                Misc.getHighlightColor(),pad);

        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_CRYOSLEEPERS)){
            tooltip.addPara("Cryosleeper survey is ready to activate.", Misc.getPositiveHighlightColor(), pad);
        }
        else {
            tooltip.addPara("Cryosleeper survey has already been used.", Misc.getNegativeHighlightColor(), pad);
        }
        addIncompatibleToTooltip(tooltip, expanded);
    }

    public void startCryosleeperScan(){
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_CRYOSLEEPERS)){
            for (SectorEntityToken cryosleeper : Global.getSector().getCustomEntitiesWithTag(Tags.CRYOSLEEPER)){
                if (tryCreateCryosleeperReportCustom(cryosleeper, log, true)){
                    Global.getSector().getMemoryWithoutUpdate().set(CAN_SCAN_CRYOSLEEPERS, false);
                }
            }
        }
    }

    public boolean tryCreateCryosleeperReportCustom(SectorEntityToken cryosleeper, Logger log, boolean showMessage){
        IntelManagerAPI intelManager = Global.getSector().getIntelManager();
        for (IntelInfoPlugin intel : intelManager.getIntel(UnremovableIntel.class)) {
            UnremovableIntel cs = (UnremovableIntel) intel;
            if (cs.getEntity() == cryosleeper) {
                return false; // report exists
            }
        }

        UnremovableIntel report = new UnremovableIntel(cryosleeper);
        report.setNew(showMessage);
        intelManager.addIntel(report, !showMessage);
        log.info("Created intel report for cryosleeper in " + cryosleeper.getStarSystem());
        return true;
    }
}
