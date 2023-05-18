package scanthosegates;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

public class LunaSettingsChangedListener implements LunaSettingsListener {
    @Override
    public void settingsChanged(String idOfModWithChangedSettings) {
        if (idOfModWithChangedSettings.equals(ModPlugin.ID)) {
            ModPlugin.readSettings();
        }
    }
}