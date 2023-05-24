package scanthosegates;

import lunalib.lunaSettings.LunaSettingsListener;

public class LunaSettingsChangedListener implements LunaSettingsListener {
    @Override
    public void settingsChanged(String idOfModWithChangedSettings) {
        if (idOfModWithChangedSettings.equals(ScannerModPlugin.ID)) {
            ScannerModPlugin.readSettings();
        }
    }
}