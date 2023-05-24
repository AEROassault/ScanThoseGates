package scanthosegates;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import scanthosegates.campaign.econ.abilities.CryosleeperScanner;
import scanthosegates.campaign.econ.abilities.GateScanner;
import scanthosegates.campaign.econ.abilities.HypershuntScanner;
import scanthosegates.campaign.listeners.RelocationListener;
import scanthosegates.campaign.listeners.SalvagingListener;

import java.util.MissingResourceException;

import static scanthosegates.LunaSettingsChangedListener.addToManagerIfNeeded;

public class ScannerModPlugin extends BaseModPlugin {
    private static final Logger log = Global.getLogger(ScannerModPlugin.class);
    static {log.setLevel(Level.ALL);}
    public static final String ID = "scan_those_gates";
    public static final String MOD_PREFIX = "stg_";
    public static final String INTEL_MEGASTRUCTURES = "Megastructures";
    public static boolean lunaLibEnabled = Global.getSettings().getModManager().isModEnabled("lunalib");

    static <T> T get(String id, Class<T> type) throws Exception {
        if (lunaLibEnabled) {
            if (type == Boolean.class) return type.cast(LunaSettings.getBoolean(ScannerModPlugin.ID, MOD_PREFIX + id));
        } else {
            if (type == Boolean.class) return type.cast(Global.getSettings().getBoolean(id));
        }
        throw new MissingResourceException("No setting found with id: " + id, type.getName(), id);
    }
    static boolean getBoolean(String id) throws Exception { return get(id, Boolean.class); }
    static void readSettings() {
        try {
            RevealAllGates = getBoolean("RevealInactiveGates");
            ActivateAllGates = getBoolean("ActivateInactiveGates");
        } catch (Exception e) {
            log.debug("Failed to read lunaSettings. Exception: " + e);
        }
    }

    public static boolean RevealAllGates = false;
    public static boolean ActivateAllGates = false;

    @Override
    public void onGameLoad(boolean newGame){
        MemoryAPI sectorMemory = Global.getSector().getMemoryWithoutUpdate();
        CharacterDataAPI characterData = Global.getSector().getCharacterData();

        if (!sectorMemory.contains(GateScanner.UNSCANNED_GATES)) {
            sectorMemory.set(GateScanner.UNSCANNED_GATES, true);
        }
        if (!sectorMemory.contains(HypershuntScanner.CAN_SCAN_HYPERSHUNTS)) {
            sectorMemory.set(HypershuntScanner.CAN_SCAN_HYPERSHUNTS, true);
        }
        if (!sectorMemory.contains(CryosleeperScanner.CAN_SCAN_CRYOSLEEPERS)) {
            sectorMemory.set(CryosleeperScanner.CAN_SCAN_CRYOSLEEPERS, true);
        }

        if (!characterData.getAbilities().contains("stg_GateScanner")) {
            characterData.addAbility("stg_GateScanner");
        }
        if (!characterData.getAbilities().contains("stg_HypershuntScanner")) {
            characterData.addAbility("stg_HypershuntScanner");
        }
        if (!characterData.getAbilities().contains("stg_CryosleeperScanner")) {
            characterData.addAbility("stg_CryosleeperScanner");
        }

        Global.getSector().getListenerManager().addListener(new RelocationListener(), true);
        Global.getSector().getListenerManager().addListener(new SalvagingListener(), true);

        readSettings();
    }

    @Override
    public void onApplicationLoad() throws Exception {
        if (lunaLibEnabled){
            addToManagerIfNeeded();
        }
    }
}
