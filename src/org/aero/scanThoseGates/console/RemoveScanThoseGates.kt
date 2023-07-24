package org.aero.scanThoseGates.console

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import org.aero.scanThoseGates.ModPlugin
import org.aero.scanThoseGates.campaign.intel.CoronalHypershuntIntel
import org.aero.scanThoseGates.campaign.intel.CryosleeperIntel
import org.apache.log4j.Level
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.BaseCommand.CommandContext
import org.lazywizard.console.BaseCommand.CommandResult
import org.lazywizard.console.CommonStrings
import org.lazywizard.console.Console

class RemoveScanThoseGates : BaseCommand {
    companion object {
        private val log = Global.getLogger(RemoveScanThoseGates::class.java)
        init { log.level = Level.ALL }
    }

    override fun runCommand(args: String, context: CommandContext): CommandResult {
        if (!context.isInCampaign) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY)
            return CommandResult.WRONG_CONTEXT
        }

        val intelManager = Global.getSector().intelManager
        val customIntel = ArrayList<IntelInfoPlugin>()

        customIntel.addAll(intelManager.getIntel(CoronalHypershuntIntel::class.java))
        customIntel.addAll(intelManager.getIntel(CryosleeperIntel::class.java))

        Console.showMessage("Removing ${customIntel.size} custom intel entries for Scan Those Gates.")
        log.info("Removing ${customIntel.size} custom intel entries for Scan Those Gates.")

        for (entry in customIntel) {
            intelManager.removeIntel(entry)
        }

        Console.showMessage("Removing abilities for Scan Those Gates.")
        log.info("Removing abilities for Scan Those Gates.")

        val characterData = Global.getSector().characterData
        if (ModPlugin.GATE_SCAN_ABILITY in characterData.abilities) {
            characterData.removeAbility(ModPlugin.GATE_SCAN_ABILITY)
        }
        if (ModPlugin.HYPERSHUNT_SCAN_ABILITY in characterData.abilities) {
            characterData.removeAbility(ModPlugin.HYPERSHUNT_SCAN_ABILITY)
        }
        if (ModPlugin.CRYOSLEEPER_SCAN_ABILITY in characterData.abilities) {
            characterData.removeAbility(ModPlugin.CRYOSLEEPER_SCAN_ABILITY)
        }

        Console.showMessage("Removing megastructure scan memory keys for Scan Those Gates.")
        log.info("Removing megastructure scan memory keys for Scan Those Gates.")

        Global.getSector().memoryWithoutUpdate.unset(ModPlugin.ALLOW_CRYOSLEEPER_SCAN)
        Global.getSector().memoryWithoutUpdate.unset(ModPlugin.ALLOW_HYPERSHUNT_SCAN)

        return CommandResult.SUCCESS
    }
}
