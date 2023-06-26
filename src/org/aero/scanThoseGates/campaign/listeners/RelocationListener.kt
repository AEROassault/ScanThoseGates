package org.aero.scanThoseGates.campaign.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener
import com.fs.starfarer.api.impl.campaign.ids.Tags
import org.aero.scanThoseGates.campaign.abilities.CryosleeperScanner.Companion.tryCreateCryosleeperReportCustom
import org.aero.scanThoseGates.campaign.abilities.HypershuntScanner.Companion.tryCreateHypershuntReport
import org.apache.log4j.Level

class RelocationListener : CurrentLocationChangedListener {
    override fun reportCurrentLocationChanged(previous: LocationAPI, current: LocationAPI) {
        for (cryo in current.getEntitiesWithTag(Tags.CRYOSLEEPER)) {
            tryCreateCryosleeperReportCustom(cryo!!, log, true, true)
        }
        for (shunt in current.getEntitiesWithTag(Tags.CORONAL_TAP)) {
            tryCreateHypershuntReport(shunt!!, log, true, true)
        }
    }

    companion object {
        private val log = Global.getLogger(RelocationListener::class.java)

        init {
            log.level = Level.ALL
        }
    }
}
