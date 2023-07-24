package org.aero.scanThoseGates

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import lunalib.lunaSettings.LunaSettings
import org.aero.scanThoseGates.campaign.abilities.GateScanner
import org.aero.scanThoseGates.campaign.listeners.RelocationListener
import org.aero.scanThoseGates.campaign.listeners.SalvagingListener
import org.aero.scanThoseGates.misc.LunaSettingsManager.addToManagerIfNeeded
import org.apache.log4j.Level

class ModPlugin : BaseModPlugin() {

    companion object Settings {
        private val log = Global.getLogger(ModPlugin::class.java)
        init { log.level = Level.ALL }

        const val ID = "scan_those_gates"
        const val LUNALIB_ID = "lunalib"

        val LUNALIB_ENABLED = Global.getSettings().modManager.isModEnabled(LUNALIB_ID)

        const val LUNA_MAJOR = 1
        const val LUNA_MINOR = 7
        const val LUNA_PATCH = 4

        var revealAllGates = false
        var activateAllGates = false

        var usableCheckInterval = 1f
        var usableCheckReportInterval = 60f

        const val GATE_SCAN_ABILITY = "stg_GateScanner"
        const val HYPERSHUNT_SCAN_ABILITY = "stg_HypershuntScanner"
        const val CRYOSLEEPER_SCAN_ABILITY = "stg_CryosleeperScanner"

        const val INTEL_MEGASTRUCTURES = "Megastructures"
        const val UNSCANNED_GATES = "\$UnscannedGatesFound"
        const val ALLOW_CRYOSLEEPER_SCAN = "\$CryosleeperScannerAllowed"
        const val ALLOW_HYPERSHUNT_SCAN = "\$HypershuntScannerAllowed"

        private fun getBoolean(id: String): Boolean {
            return if (LUNALIB_ENABLED) (LunaSettings.getBoolean(ID, id) ?: false) else Global.getSettings().getBoolean(id)
        }

        private fun getFloat(id: String, defaultValue: Float): Float {
            return if (LUNALIB_ENABLED) (LunaSettings.getFloat(ID, id) ?: defaultValue) else Global.getSettings().getFloat(id)
        }

        fun readSettings() {
            revealAllGates = getBoolean("RevealInactiveGates")
            activateAllGates = getBoolean("ActivateInactiveGates")

            usableCheckInterval = getFloat("GateScanUsableCheckInterval", 1f)
            usableCheckReportInterval = getFloat("GateScanDiagnosticsInterval", 60f)
        }
    }

    override fun onGameLoad(newGame: Boolean) {
        val sectorMemory = Global.getSector().memoryWithoutUpdate
        val characterData = Global.getSector().characterData
        val playerAbilities = characterData.abilities + Global.getSector().playerPerson.stats.grantedAbilityIds

        if (UNSCANNED_GATES !in sectorMemory) {
            sectorMemory[UNSCANNED_GATES] = false
        }
        if (ALLOW_HYPERSHUNT_SCAN !in sectorMemory) {
            sectorMemory[ALLOW_HYPERSHUNT_SCAN] = true
        }
        if (ALLOW_CRYOSLEEPER_SCAN !in sectorMemory) {
            sectorMemory[ALLOW_CRYOSLEEPER_SCAN] = true
        }

        if (GATE_SCAN_ABILITY !in playerAbilities) {
            characterData.addAbility(GATE_SCAN_ABILITY)
        }
        if (HYPERSHUNT_SCAN_ABILITY !in playerAbilities) {
            characterData.addAbility(HYPERSHUNT_SCAN_ABILITY)
        }
        if (CRYOSLEEPER_SCAN_ABILITY !in playerAbilities) {
            characterData.addAbility(CRYOSLEEPER_SCAN_ABILITY)
        }

        Global.getSector().listenerManager.addListener(RelocationListener(), true)
        Global.getSector().listenerManager.addListener(SalvagingListener(), true)

        readSettings()

        GateScanner.secondsSinceLastCheck = usableCheckInterval
        GateScanner.secondsSinceLastReport = usableCheckReportInterval
    }

    override fun onApplicationLoad() {
        if (LUNALIB_ENABLED) {
            if (requiredLunaLibVersionPresent()) {
                addToManagerIfNeeded()
            } else {
                throw RuntimeException("Using LunaLib with ScanThoseGates requires at least version " +
                        "$LUNA_MAJOR.$LUNA_MINOR.$LUNA_PATCH of LunaLib. Update your LunaLib."
                )
            }
        }
    }

    private fun requiredLunaLibVersionPresent(): Boolean {
        val version = Global.getSettings().modManager.getModSpec(LUNALIB_ID).version
        log.info("LunaLib Version: $version")
        val splitVersion = version.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            .split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val installedMajor = splitVersion[0].toInt()
        val installedMinor = splitVersion[1].toInt()
        val installedPatch = splitVersion[2].toInt()

        if (installedMajor > LUNA_MAJOR) {
            return true
        } else if (installedMajor < LUNA_MAJOR) {
            return false
        }
        if (installedMinor > LUNA_MINOR) {
            return true
        }
        return (installedMinor >= LUNA_MINOR && installedPatch >= LUNA_PATCH)
    }

}
