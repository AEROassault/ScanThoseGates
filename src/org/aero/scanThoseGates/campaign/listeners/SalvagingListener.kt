package org.aero.scanThoseGates.campaign.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.listeners.DiscoverEntityListener
import org.aero.scanThoseGates.campaign.abilities.CryosleeperScanner.Companion.tryCreateCryosleeperReportCustom
import org.aero.scanThoseGates.campaign.abilities.HypershuntScanner.Companion.tryCreateHypershuntReport
import org.apache.log4j.Level

class SalvagingListener : DiscoverEntityListener {
    override fun reportEntityDiscovered(entity: SectorEntityToken) {
        tryCreateCryosleeperReportCustom(entity, log, true, true)
        tryCreateHypershuntReport(entity, log, true, true)
    }

    companion object {
        private val log = Global.getLogger(SalvagingListener::class.java)

        init {
            log.level = Level.ALL
        }
    }
}
