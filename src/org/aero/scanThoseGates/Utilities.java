package org.aero.scanThoseGates;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.Misc;

public class Utilities {

    public static String getSystemNameOrHyperspace(SectorEntityToken token) {
        if (token.getStarSystem() != null) {
            return token.getStarSystem().getNameWithLowercaseType();
        }


        float maxRangeLY = 2f;

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float dist = Misc.getDistanceLY(token.getLocationInHyperspace(), system.getLocation());
            if (dist <= maxRangeLY) {
                return "Hyperspace near " + system.getName();
            }
        }

        return "Hyperspace";
    }
}