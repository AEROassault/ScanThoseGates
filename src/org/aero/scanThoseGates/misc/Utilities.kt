package org.aero.scanThoseGates.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.util.Misc
import org.aero.scanThoseGates.ModPlugin
import org.apache.log4j.Level

object Utilities {

    private val log = Global.getLogger(ModPlugin::class.java)
    init { log.level = Level.ALL }

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
