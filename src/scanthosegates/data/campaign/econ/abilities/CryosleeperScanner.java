package scanthosegates.data.campaign.econ.abilities;

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
import scanthosegates.data.campaign.intel.CryosleeperIntel;


public class CryosleeperScanner extends BaseDurationAbility {
    public static String CAN_SCAN_CRYOSLEEPERS = "$CryosleeperScannerAllowed";
    private static final Logger log = Global.getLogger(scanthosegates.data.campaign.econ.abilities.CryosleeperScanner.class);
    static {log.setLevel(Level.ALL);}

    @Override
    protected void activateImpl() {

    }

    @Override
    protected void applyEffect(float amount, float level) {
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_CRYOSLEEPERS)){
            for (SectorEntityToken cryosleeper : Global.getSector().getCustomEntitiesWithTag(Tags.CRYOSLEEPER)){
                if (tryCreateCryosleeperReportCustom(cryosleeper, log, true)
                        && Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_CRYOSLEEPERS)){
                    Global.getSector().getMemoryWithoutUpdate().set(CAN_SCAN_CRYOSLEEPERS, false);
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
        return Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_CRYOSLEEPERS);
    }

    @Override
    public boolean hasTooltip(){return true;}

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        tooltip.addTitle("Remote Cryosleeper Survey");
        float pad = 10f;
        tooltip.addPara("Remotely surveys all Cryosleepers in the sector and adds them to the intel screen.", pad);

        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_CRYOSLEEPERS)){
            tooltip.addPara("Cryosleeper survey is ready to activate.", Misc.getPositiveHighlightColor(), pad);
        }
        else {
            tooltip.addPara("Cryosleeper survey has already been used.", Misc.getNegativeHighlightColor(), pad);
        }
        addIncompatibleToTooltip(tooltip, expanded);
    }

    public static boolean tryCreateCryosleeperReportCustom(SectorEntityToken cryosleeper, Logger log, boolean showMessage) {
        IntelManagerAPI intelManager = Global.getSector().getIntelManager();
        for (IntelInfoPlugin intel : intelManager.getIntel(CryosleeperIntel.class)) {
            CryosleeperIntel cs = (CryosleeperIntel) intel;
            if (cs.getEntity() == cryosleeper) {
                return false; // report exists
            }
        }

        CryosleeperIntel report = new CryosleeperIntel(cryosleeper);
        report.setNew(showMessage);
        intelManager.addIntel(report, !showMessage);
        log.info("Created intel report for cryosleeper in " + cryosleeper.getStarSystem());

        return true;
    }
}
