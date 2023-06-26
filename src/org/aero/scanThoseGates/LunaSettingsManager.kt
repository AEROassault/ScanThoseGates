package org.aero.scanThoseGates

import lunalib.lunaSettings.LunaSettings.addSettingsListener
import lunalib.lunaSettings.LunaSettings.hasSettingsListenerOfClass
import lunalib.lunaSettings.LunaSettingsListener
import org.aero.scanThoseGates.ModPlugin.Settings

object LunaSettingsManager : LunaSettingsListener {
    override fun settingsChanged(modID: String) {
        if (modID == ModPlugin.ID) {
            ModPlugin.readSettings()
        }
    }

    fun addToManagerIfNeeded() {
        if (Settings.lunaLibEnabled && !hasSettingsListenerOfClass(LunaSettingsManager::class.java)) {
            addSettingsListener(LunaSettingsManager)
        }
    }
}