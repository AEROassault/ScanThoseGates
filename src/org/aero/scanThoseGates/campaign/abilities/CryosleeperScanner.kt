package org.aero.scanThoseGates.campaign.abilities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.aero.scanThoseGates.ModPlugin.Settings.CAN_SCAN_CRYOSLEEPERS
import org.aero.scanThoseGates.campaign.intel.CryosleeperIntel
import org.apache.log4j.Level
import org.apache.log4j.Logger

class CryosleeperScanner : BaseDurationAbility() {

    override fun applyEffect(amount: Float, level: Float) {
        if (Global.getSector().memoryWithoutUpdate.getBoolean(CAN_SCAN_CRYOSLEEPERS)) {
            for (cryosleeper in Global.getSector().getCustomEntitiesWithTag(Tags.CRYOSLEEPER)) {
                if (tryCreateCryosleeperReportCustom(cryosleeper, log, showMessage = true, listener = false)
                    && Global.getSector().memoryWithoutUpdate.getBoolean(CAN_SCAN_CRYOSLEEPERS)
                ) {
                    Global.getSector().memoryWithoutUpdate[CAN_SCAN_CRYOSLEEPERS] = false
                }
            }
        }
    }

    override fun isUsable(): Boolean {
        return Global.getSector().memoryWithoutUpdate.getBoolean(CAN_SCAN_CRYOSLEEPERS)
    }
    override fun hasTooltip(): Boolean {
        return true
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean) {
        val fleet = fleet ?: return
        tooltip.addTitle("Remote Cryosleeper Survey")
        val pad = 10f
        tooltip.addPara("Remotely surveys all Cryosleepers in the sector and adds them to the intel screen.", pad)
        if (Global.getSector().memoryWithoutUpdate.getBoolean(CAN_SCAN_CRYOSLEEPERS)) {
            tooltip.addPara("Cryosleeper survey is ready to activate.", Misc.getPositiveHighlightColor(), pad)
        } else {
            tooltip.addPara("Cryosleeper survey has already been used.", Misc.getNegativeHighlightColor(), pad)
        }
        addIncompatibleToTooltip(tooltip, expanded)
    }

    override fun activateImpl() {}
    override fun deactivateImpl() {}
    override fun cleanupImpl() {}

    companion object {
        private val log = Global.getLogger(CryosleeperScanner::class.java)
        init { log.level = Level.ALL }

        fun tryCreateCryosleeperReportCustom(cryosleeper: SectorEntityToken, log: Logger, showMessage: Boolean, listener: Boolean): Boolean {
            if ((!cryosleeper.hasTag(Tags.CRYOSLEEPER) || cryosleeper.hasSensorProfile() || cryosleeper.isDiscoverable) && listener) {
                return false
            }
            val intelManager = Global.getSector().intelManager
            for (intel in intelManager.getIntel(CryosleeperIntel::class.java)) {
                val cs = intel as CryosleeperIntel
                if (cs.entity === cryosleeper) {
                    return false // report exists
                }
            }
            val report = CryosleeperIntel(cryosleeper)
            report.isNew = showMessage
            intelManager.addIntel(report, !showMessage)
            log.info("Created intel report for cryosleeper in ${cryosleeper.starSystem}")
            return true
        }
    }
}
