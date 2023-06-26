package org.aero.scanThoseGates

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.Misc

object Utilities {
    @JvmStatic
    fun getSystemNameOrHyperspace(token: SectorEntityToken): String {
        if (token.starSystem != null) {
            return token.starSystem.nameWithLowercaseType
        }
        val maxRangeLY = 2f
        for (system in Global.getSector().starSystems) {
            val dist = Misc.getDistanceLY(token.locationInHyperspace, system.location)
            if (dist <= maxRangeLY) {
                return "Hyperspace near " + system.name
            }
        }
        return "Hyperspace"
    }
}
