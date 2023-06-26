package org.aero.scanThoseGates.campaign.abilities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.aero.scanThoseGates.ModPlugin.Settings.CAN_SCAN_HYPERSHUNTS
import org.aero.scanThoseGates.campaign.intel.CoronalHypershuntIntel
import org.apache.log4j.Level
import org.apache.log4j.Logger

class HypershuntScanner : BaseDurationAbility() {

    override fun applyEffect(amount: Float, level: Float) {
        if (Global.getSector().memoryWithoutUpdate.getBoolean(CAN_SCAN_HYPERSHUNTS)) {
            for (hypershunt in Global.getSector().getCustomEntitiesWithTag(Tags.CORONAL_TAP)) {
                if (tryCreateHypershuntReport(hypershunt, log, true, false)
                    && Global.getSector().memoryWithoutUpdate.getBoolean(CAN_SCAN_HYPERSHUNTS)
                ) {
                    Global.getSector().memoryWithoutUpdate[CAN_SCAN_HYPERSHUNTS] = false
                }
            }
        }
    }

    override fun isUsable(): Boolean { return Global.getSector().memoryWithoutUpdate.getBoolean(CAN_SCAN_HYPERSHUNTS) }
    override fun hasTooltip(): Boolean { return true }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean) {
        val fleet = fleet ?: return
        tooltip.addTitle("Remote Hypershunt Survey")
        val pad = 10f
        tooltip.addPara(
            "Remotely surveys all Coronal Hypershunts in the sector and adds them to the intel screen.",
            pad
        )
        if (Global.getSector().memoryWithoutUpdate.getBoolean(CAN_SCAN_HYPERSHUNTS)) {
            tooltip.addPara("Hypershunt survey is ready to activate.", Misc.getPositiveHighlightColor(), pad)
            tooltip.addPara(
                "WARNING. Using this ability will reveal the basic layout of systems containing a hypershunt.",
                Misc.getHighlightColor(),
                pad
            )
        } else {
            tooltip.addPara("Hypershunt survey has already been used.", Misc.getNegativeHighlightColor(), pad)
        }
        addIncompatibleToTooltip(tooltip, expanded)
    }

    override fun activateImpl() {}
    override fun deactivateImpl() {}
    override fun cleanupImpl() {}

    companion object {
        private val log = Global.getLogger(HypershuntScanner::class.java)
        init { log.level = Level.ALL }

        fun tryCreateHypershuntReport(hypershunt: SectorEntityToken, log: Logger, showMessage: Boolean, listener: Boolean): Boolean {
            if ((!hypershunt.hasTag(Tags.CORONAL_TAP) || hypershunt.hasSensorProfile() || hypershunt.isDiscoverable) && listener) {
                return false
            }
            val intelManager = Global.getSector().intelManager
            for (intel in intelManager.getIntel(CoronalHypershuntIntel::class.java)) {
                val hs = intel as CoronalHypershuntIntel
                if (hs.entity === hypershunt) {
                    return false // report exists
                }
            }
            val report = CoronalHypershuntIntel(hypershunt)
            report.isNew = showMessage
            intelManager.addIntel(report, !showMessage)
            log.info("Created intel report for hypershunt in ${hypershunt.starSystem}")
            return true
        }
    }
}
