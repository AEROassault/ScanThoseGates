package scanthosegates;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import scanthosegates.campaign.econ.abilities.CryosleeperScanner;
import scanthosegates.campaign.econ.abilities.GateScanner;
import scanthosegates.campaign.econ.abilities.HypershuntScanner;
import scanthosegates.campaign.listeners.RelocationListener;
import scanthosegates.campaign.listeners.SalvagingListener;

import java.util.MissingResourceException;

public class ScannerModPlugin extends BaseModPlugin {
    private static final Logger log = Global.getLogger(ScannerModPlugin.class);
    static {log.setLevel(Level.ALL);}
    public static final String ID = "scan_those_gates";
    public static final String PREFIX = "stg_";
    static final String LUNALIB_ID = "lunalib";
    public static final String INTEL_MEGASTRUCTURES = "Megastructures";
    static <T> T get(String id, Class<T> type) throws Exception {
        if (Global.getSettings().getModManager().isModEnabled(LUNALIB_ID)) {
            if (type == Boolean.class) return type.cast(lunalib.lunaSettings.LunaSettings.getBoolean(ScannerModPlugin.ID, PREFIX + id));
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
    public static boolean
            RevealAllGates = false,
            ActivateAllGates = false;

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
    }

    @Override
    public void onApplicationLoad() {
        if (Global.getSettings().getModManager().isModEnabled(LUNALIB_ID)) {
            lunalib.lunaSettings.LunaSettings.addSettingsListener(new LunaSettingsChangedListener());
        }
    }
}
