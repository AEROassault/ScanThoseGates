package data.campaign.econ.abilities;

import CaptainsLog.campaign.intel.SalvageableIntel;
import CaptainsLog.scripts.Utils;
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

import static CaptainsLog.campaign.intel.SalvageableIntel.IGNORE_SALVAGEABLE_MEM_FLAG;

public class HypershuntScanner extends BaseDurationAbility {
    public static String CAN_SCAN_HYPERSHUNTS = "$HypershuntScannerAllowed";

    private static final Logger log = Global.getLogger(data.campaign.econ.abilities.HypershuntScanner.class);
    static {log.setLevel(Level.ALL);}

    @Override
    protected void activateImpl() {

    }

    @Override
    protected void applyEffect(float amount, float level) {
        startHypershuntScan();
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
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_HYPERSHUNTS);
    }

    @Override
    public boolean hasTooltip(){return true;}

    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        tooltip.addTitle("Remote Hypershunt Survey");
        float pad = 10f;
        tooltip.addPara("Remotely surveys all Coronal Hypershunts in the sector and adds them to the intel screen.",
                Misc.getHighlightColor(), pad);

        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_HYPERSHUNTS)) {
            tooltip.addPara("Hypershunt survey is ready to activate.", Misc.getPositiveHighlightColor(), pad);
        }
        else {
            tooltip.addPara("Hypershunt survey has already been used.", Misc.getNegativeHighlightColor(), pad);
        }
        addIncompatibleToTooltip(tooltip, expanded);
    }

    public void startHypershuntScan(){
        if (Global.getSector().getMemoryWithoutUpdate().getBoolean(CAN_SCAN_HYPERSHUNTS)){
            for (SectorEntityToken hypershunt : Global.getSector().getCustomEntitiesWithTag(Tags.CORONAL_TAP)){
                if (tryCreateSalvageableReportCustom(hypershunt, log, true)){
                    Global.getSector().getMemoryWithoutUpdate().set(CAN_SCAN_HYPERSHUNTS, false);
                }
            }
        }
    }

    public static boolean tryCreateSalvageableReportCustom(SectorEntityToken token, Logger log, boolean showMessage) {
        if (token == null || !token.hasTag(Tags.SALVAGEABLE) || !token.isAlive()) {
            return false;
        }

        IntelManagerAPI intelManager = Global.getSector().getIntelManager();
        for (IntelInfoPlugin salvage : intelManager.getIntel(SalvageableIntel.class)) {
            SalvageableIntel rs = (SalvageableIntel) salvage;
            if (rs.getEntity() == token) {
                return false;
            }
        }

        SalvageableIntel report = new SalvageableIntel(token);
        report.setNew(showMessage);
        intelManager.addIntel(report, !showMessage);
        log.info("Created report for " + token.getFullName() + " in " + Utils.getSystemNameOrHyperspace(token));
        return true;
    }
}
