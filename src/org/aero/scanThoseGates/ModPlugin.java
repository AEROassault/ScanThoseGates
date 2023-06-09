package org.aero.scanThoseGates;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.aero.scanThoseGates.campaign.abilities.CryosleeperScanner;
import org.aero.scanThoseGates.campaign.abilities.GateScanner;
import org.aero.scanThoseGates.campaign.abilities.HypershuntScanner;
import org.aero.scanThoseGates.campaign.listeners.RelocationListener;
import org.aero.scanThoseGates.campaign.listeners.SalvagingListener;

import java.util.MissingResourceException;

import static org.aero.scanThoseGates.LunaSettingsChangedListener.addToManagerIfNeeded;

public class ModPlugin extends BaseModPlugin {
    private static final Logger log = Global.getLogger(ModPlugin.class);
    static {log.setLevel(Level.ALL);}
    public static final String ID = "scan_those_gates";
    public static final String MOD_PREFIX = "stg_";
    public static final String INTEL_MEGASTRUCTURES = "Megastructures";
    public static final String LUNALIB_ID = "lunalib";
    public static boolean lunaLibEnabled = Global.getSettings().getModManager().isModEnabled(LUNALIB_ID);
    public static final int LUNA_MAJOR = 1, LUNA_MINOR = 7, LUNA_PATCH = 4;

    static <T> T get(String id, Class<T> type) throws Exception {
        if (lunaLibEnabled) {
            if (type == Boolean.class) return type.cast(LunaSettings.getBoolean(ModPlugin.ID, MOD_PREFIX + id));
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
    public void onGameLoad(boolean newGame) {
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
    public void onApplicationLoad() {
        if (lunaLibEnabled){
            if (requiredLunaLibVersionPresent()) {
                addToManagerIfNeeded();
            } else {
                throw new RuntimeException("Using LunaLib with this mod requires at least version "
                        + LUNA_MAJOR + "." + LUNA_MINOR + "." + LUNA_PATCH + " of LunaLib. Update your LunaLib, or else...");
            }
        }
    }

    public static boolean requiredLunaLibVersionPresent() {
        String version = Global.getSettings().getModManager().getModSpec(LUNALIB_ID).getVersion();
        log.info("LunaLib Version: " + version);
        String[] temp = version.split("\\.");
        if (Integer.parseInt(temp[0]) < LUNA_MAJOR) return false;
        if (Integer.parseInt(temp[1]) < LUNA_MINOR) return false;
        if (Integer.parseInt(temp[2]) < LUNA_PATCH) return false;
        return true;
    }
}
