package org.aero.scanThoseGates;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

import static org.aero.scanThoseGates.ModPlugin.lunaLibEnabled;

public class LunaSettingsManager implements LunaSettingsListener {
    @Override
    public void settingsChanged(String idOfModWithChangedSettings) {
        if (idOfModWithChangedSettings.equals(ModPlugin.ID)) {
            ModPlugin.readSettings();
        }
    }

    public static void addToManagerIfNeeded() {
        if(lunaLibEnabled && !LunaSettings.hasSettingsListenerOfClass(LunaSettingsManager.class)) {
            LunaSettings.addSettingsListener(new LunaSettingsManager());
        }
    }
}