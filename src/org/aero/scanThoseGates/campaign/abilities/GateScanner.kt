package org.aero.scanThoseGates.campaign.abilities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.misc.GateIntel
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateCMD
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.aero.scanThoseGates.ModPlugin.Settings.ActivateAllGates
import org.aero.scanThoseGates.ModPlugin.Settings.RevealAllGates
import org.aero.scanThoseGates.ModPlugin.Settings.UNSCANNED_GATES
import org.apache.log4j.Level
import kotlin.math.pow

class GateScanner : BaseDurationAbility() {
    companion object {
        private val log = Global.getLogger(GateScanner::class.java)
        init { log.level = Level.ALL }

        var systemsWithMarkets = HashSet<LocationAPI>()
        var gateScanPrimed = false

        const val checksReportInterval = 60f
        var secondsSinceLastReport = 60f

        const val secondsBetweenChecks = 1f
        var secondsSinceLastCheck = 1f
    }
    override fun applyEffect(amount: Float, level: Float) {
        log.info("Scan Those Gates settings variable logging: RevealAllGates = $RevealAllGates, ActivateAllGates = $ActivateAllGates")
        if (Global.getSector().memoryWithoutUpdate.getBoolean(UNSCANNED_GATES)) {
            val startTime = System.nanoTime()
            generateMarketSystemsHashset()
            var gateStatusString = "null"
            var gateIntelString = "null"
            for (gate in Global.getSector().getCustomEntitiesWithTag(Tags.GATE)) {
                val gateScanStatus = gate.memoryWithoutUpdate.getBoolean(GateEntityPlugin.GATE_SCANNED)
                var revealThatGate = false
                try {
                    if (systemsWithMarkets.contains(gate.containingLocation) && !gateScanStatus || ActivateAllGates && !gateScanStatus) {
                        gate.memory[GateEntityPlugin.GATE_SCANNED] = true
                        GateCMD.notifyScanned(gate)
                        gateStatusString = "has been activated"
                        revealThatGate = true
                    } else if (gateScanStatus) {
                        gateStatusString = "has already been activated"
                    } else {
                        gateStatusString = "will not been activated"
                    }
                } catch (cannotScanGate: Exception) {
                    gateStatusString = " is broken. Exception: $cannotScanGate."
                    revealThatGate = true
                } finally {
                    try {
                        if (gateIntelDoesNotExist(gate)) {
                            if (revealThatGate || RevealAllGates) {
                                Global.getSector().intelManager.addIntel(GateIntel(gate))
                                gateIntelString = "an intel entry has been created"
                            } else {
                                gateIntelString = "an intel entry will not be created"
                            }
                        } else {
                            gateIntelString = "already has an intel entry"
                        }
                    } catch (cannotAddGateIntel: Exception) {
                        log.debug("${gate.name} in ${gate.containingLocation.name} somehow broke the intel system. Exception: $cannotAddGateIntel")
                    }
                    log.info("${gate.name} in ${gate.containingLocation.name} $gateStatusString and $gateIntelString.")
                }
            }
            Global.getSector().memoryWithoutUpdate[UNSCANNED_GATES] = false
            systemsWithMarkets.clear()

            val elapsedTime = System.nanoTime() - startTime

            log.info("GateScanner took ${elapsedTime / 10.0.pow(9.0)} seconds " +
                        "(${elapsedTime / 10.0.pow(6.0)} milliseconds or " +
                        "$elapsedTime nanoseconds) to execute the gate scan.")
        }
    }

    override fun advance(amount: Float) {
        super.advance(amount)
        secondsSinceLastCheck += amount
        secondsSinceLastReport += amount
        if (secondsSinceLastCheck > secondsBetweenChecks) {
            val startUsableCheck = System.nanoTime()
            checkForGates()
            secondsSinceLastCheck = 0f
            val endUsableCheck = System.nanoTime() - startUsableCheck
            if (secondsSinceLastReport > checksReportInterval) {
                systemsWithMarkets.clear()
                log.info("CheckForGates took ${endUsableCheck / 10.0.pow(9.0)} seconds " +
                        "(${endUsableCheck / 10.0.pow(6.0)} milliseconds or " +
                        "$endUsableCheck nanoseconds) to complete.")
                secondsSinceLastReport = 0f
            }
        }
    }

    override fun isUsable(): Boolean {
        return (Global.getSector().memoryWithoutUpdate.getBoolean(GateEntityPlugin.CAN_SCAN_GATES)
                && Global.getSector().memoryWithoutUpdate.getBoolean(GateEntityPlugin.GATES_ACTIVE)
                && Global.getSector().memoryWithoutUpdate.getBoolean(UNSCANNED_GATES))
    }

    override fun hasTooltip(): Boolean {
        return true
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean) {
        gateScanPrimed = (Global.getSector().memoryWithoutUpdate.getBoolean(GateEntityPlugin.CAN_SCAN_GATES)
                && Global.getSector().memoryWithoutUpdate.getBoolean(GateEntityPlugin.GATES_ACTIVE))
        val fleet = fleet ?: return
        tooltip.addTitle("Remote Gate Scan")
        val pad = 10f
        if (RevealAllGates && !ActivateAllGates) {
            tooltip.addPara("Scans all gates located in systems with at least one non-hidden market " +
                        "and adds all gates to the intel screen, regardless of market presence in the gate's system.", pad
            )
        } else if (ActivateAllGates) {
            tooltip.addPara("Scans all gates regardless of market presence in the gate's system.", pad)
        } else {
            tooltip.addPara("Scans all gates located in systems with at least one non-hidden market " +
                        "and adds them to the intel screen.", 10f
            )
        }
        if (!gateScanPrimed) {
            tooltip.addPara(
                "Cannot activate gates yet, the Janus Device has not been acquired.",
                Misc.getNegativeHighlightColor(), pad
            )
        } else if (!Global.getSector().memoryWithoutUpdate.getBoolean(UNSCANNED_GATES)) {
            tooltip.addPara(
                "The Janus Device has been acquired, but there are no gates available to scan.",
                Misc.getNegativeHighlightColor(), pad
            )
        } else {
            tooltip.addPara(
                "The Janus Device has been acquired and there are gates available to scan.",
                Misc.getPositiveHighlightColor(), pad
            )
        }
        addIncompatibleToTooltip(tooltip, expanded)
    }

    private fun gateIntelDoesNotExist(gate: SectorEntityToken): Boolean {
        for (intel in Global.getSector().intelManager.getIntel(GateIntel::class.java)) {
            val gi = intel as GateIntel
            if (gi.gate === gate) {
                return false
            }
        }
        return true
    }

    private fun generateMarketSystemsHashset() {
        for (systemWithMarket in Global.getSector().economy.starSystemsWithMarkets) {
            if (!systemsWithMarkets.contains(systemWithMarket)) {
                for (market in Global.getSector().economy.getMarkets(systemWithMarket)) {
                    if (!market.isHidden) {
                        systemsWithMarkets.add(systemWithMarket)
                        break
                    }
                }
            }
        }
    }

    private fun checkForGates() {
        generateMarketSystemsHashset()
        for (gate in Global.getSector().getCustomEntitiesWithTag(Tags.GATE)) {
            if (!gate.memoryWithoutUpdate.getBoolean(GateEntityPlugin.GATE_SCANNED)) {
                if (ActivateAllGates) {
                    Global.getSector().memoryWithoutUpdate[UNSCANNED_GATES] = true
                    return
                } else if (RevealAllGates && gateIntelDoesNotExist(gate)) {
                    Global.getSector().memoryWithoutUpdate[UNSCANNED_GATES] = true
                    return
                } else if (systemsWithMarkets.contains(gate.containingLocation)) {
                    Global.getSector().memoryWithoutUpdate[UNSCANNED_GATES] = true
                    return
                }
            }
        }
    }

    override fun activateImpl() {}
    override fun deactivateImpl() {}
    override fun cleanupImpl() {}

}
