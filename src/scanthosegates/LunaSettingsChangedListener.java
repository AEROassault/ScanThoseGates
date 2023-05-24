package scanthosegates;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

import static scanthosegates.ScannerModPlugin.lunaLibEnabled;

public class LunaSettingsChangedListener implements LunaSettingsListener {
    @Override
    public void settingsChanged(String idOfModWithChangedSettings) {
        if (idOfModWithChangedSettings.equals(ScannerModPlugin.ID)) {
            ScannerModPlugin.readSettings();
        }
    }

    public static void addToManagerIfNeeded() {
        if(lunaLibEnabled && !LunaSettings.hasSettingsListenerOfClass(LunaSettingsChangedListener.class)) {
            LunaSettings.addSettingsListener(new LunaSettingsChangedListener());
        }
    }
}