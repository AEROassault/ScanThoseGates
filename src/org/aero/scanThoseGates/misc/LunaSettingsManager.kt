package org.aero.scanThoseGates.misc

import lunalib.lunaSettings.LunaSettings.addSettingsListener
import lunalib.lunaSettings.LunaSettings.hasSettingsListenerOfClass
import lunalib.lunaSettings.LunaSettingsListener
import org.aero.scanThoseGates.ModPlugin.Settings
import org.aero.scanThoseGates.ModPlugin.Settings.ID
import org.aero.scanThoseGates.ModPlugin.Settings.readSettings

object LunaSettingsManager : LunaSettingsListener {
    override fun settingsChanged(modID: String) {
        if (modID == ID) {
            readSettings()
        }
    }

    fun addToManagerIfNeeded() {
        if (Settings.lunaLibEnabled && !hasSettingsListenerOfClass(LunaSettingsManager::class.java)) {
            addSettingsListener(LunaSettingsManager)
        }
    }
}