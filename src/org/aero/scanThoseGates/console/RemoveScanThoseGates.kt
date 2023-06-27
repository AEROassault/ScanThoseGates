package org.aero.scanThoseGates.console

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import org.aero.scanThoseGates.campaign.intel.CoronalHypershuntIntel
import org.aero.scanThoseGates.campaign.intel.CryosleeperIntel
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.BaseCommand.CommandContext
import org.lazywizard.console.BaseCommand.CommandResult
import org.lazywizard.console.CommonStrings
import org.lazywizard.console.Console

class RemoveScanThoseGates : BaseCommand {
    override fun runCommand(args: String, context: CommandContext): CommandResult {
        if (!context.isInCampaign) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY)
            return CommandResult.WRONG_CONTEXT
        }

        val intelManager = Global.getSector().intelManager
        val all = ArrayList<IntelInfoPlugin>()

        all.addAll(intelManager.getIntel(CoronalHypershuntIntel::class.java))
        all.addAll(intelManager.getIntel(CryosleeperIntel::class.java))

        Console.showMessage("Removing ${all.size} custom intel entries for Scan Those Gates.")
        for (i in all) {
            intelManager.removeIntel(i)
        }
        val characterData = Global.getSector().characterData
        if (characterData.abilities.contains("stg_GateScanner")) {
            characterData.removeAbility("stg_GateScanner")
        }
        if (characterData.abilities.contains("stg_HypershuntScanner")) {
            characterData.removeAbility("stg_HypershuntScanner")
        }
        if (characterData.abilities.contains("stg_CryosleeperScanner")) {
            characterData.removeAbility("stg_CryosleeperScanner")
        }
        return CommandResult.SUCCESS
    }
}
