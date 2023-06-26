package org.aero.scanThoseGates

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import lunalib.lunaSettings.LunaSettings
import org.aero.scanThoseGates.campaign.listeners.RelocationListener
import org.aero.scanThoseGates.campaign.listeners.SalvagingListener
import org.apache.log4j.Level

class ModPlugin : BaseModPlugin() {

    companion object Settings {
        private val log = Global.getLogger(ModPlugin::class.java)
        init { log.level = Level.ALL }

        const val ID = "scan_those_gates"
        const val LUNALIB_ID = "lunalib"

        var lunaLibEnabled = Global.getSettings().modManager.isModEnabled(LUNALIB_ID)

        const val LUNA_MAJOR = 1
        const val LUNA_MINOR = 7
        const val LUNA_PATCH = 4

        var RevealAllGates = false
        var ActivateAllGates = false

        const val gateAbilityString = "stg_GateScanner"
        const val hypershuntAbilityString = "stg_HypershuntScanner"
        const val cryosleeperAbilitystring = "stg_CryosleeperScanner"

        const val INTEL_MEGASTRUCTURES = "Megastructures"
        const val UNSCANNED_GATES = "\$UnscannedGatesFound"
        const val CAN_SCAN_HYPERSHUNTS = "\$HypershuntScannerAllowed"
        const val CAN_SCAN_CRYOSLEEPERS = "\$CryosleeperScannerAllowed"

        private fun getBoolean(id: String): Boolean { return LunaSettings.getBoolean(ID, id) ?: Global.getSettings().getBoolean(id) }

        fun readSettings() {
            RevealAllGates = getBoolean("RevealInactiveGates")
            ActivateAllGates = getBoolean("ActivateInactiveGates")
        }

    }
    override fun onGameLoad(newGame: Boolean) {
        val sectorMemory = Global.getSector().memoryWithoutUpdate
        val characterData = Global.getSector().characterData
        val playerAbilities = characterData.abilities + Global.getSector().playerPerson.stats.grantedAbilityIds

        if (UNSCANNED_GATES !in sectorMemory) {
            sectorMemory[UNSCANNED_GATES] = true
        }
        if (CAN_SCAN_HYPERSHUNTS !in sectorMemory) {
            sectorMemory[CAN_SCAN_HYPERSHUNTS] = true
        }
        if (CAN_SCAN_CRYOSLEEPERS !in sectorMemory) {
            sectorMemory[CAN_SCAN_CRYOSLEEPERS] = true
        }

        if (gateAbilityString !in playerAbilities) {
            characterData.addAbility(gateAbilityString)
        }
        if (hypershuntAbilityString !in playerAbilities) {
            characterData.addAbility(hypershuntAbilityString)
        }
        if (cryosleeperAbilitystring !in playerAbilities) {
            characterData.addAbility(cryosleeperAbilitystring)
        }

        Global.getSector().listenerManager.addListener(RelocationListener(), true)
        Global.getSector().listenerManager.addListener(SalvagingListener(), true)

        readSettings()
    }

    override fun onApplicationLoad() {
        if (lunaLibEnabled) {
            if (requiredLunaLibVersionPresent()) {
                LunaSettingsManager.addToManagerIfNeeded()
            } else {
                throw RuntimeException("Using LunaLib with this mod requires at least version " +
                        "$LUNA_MAJOR.$LUNA_MINOR.$LUNA_PATCH of LunaLib. Update your LunaLib, or else..."
                )
            }
        }
    }

    private fun requiredLunaLibVersionPresent(): Boolean {
        val version = Global.getSettings().modManager.getModSpec(LUNALIB_ID).version
        log.info("LunaLib Version: $version")
        val temp = version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (temp[0].toInt() < LUNA_MAJOR) return false
        if (temp[1].toInt() < LUNA_MINOR) return false
        return temp[2].toInt() >= LUNA_PATCH
    }

}
