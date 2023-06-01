package org.aero.scanthosegates;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

import static org.aero.scanthosegates.ModPlugin.lunaLibEnabled;

public class LunaSettingsChangedListener implements LunaSettingsListener {
    @Override
    public void settingsChanged(String idOfModWithChangedSettings) {
        if (idOfModWithChangedSettings.equals(ModPlugin.ID)) {
            ModPlugin.readSettings();
        }
    }

    public static void addToManagerIfNeeded() {
        if(lunaLibEnabled && !LunaSettings.hasSettingsListenerOfClass(LunaSettingsChangedListener.class)) {
            LunaSettings.addSettingsListener(new LunaSettingsChangedListener());
        }
    }
}