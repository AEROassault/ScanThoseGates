package scanthosegates.data.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import scanthosegates.data.campaign.econ.abilities.CryosleeperScanner;
import scanthosegates.data.campaign.econ.abilities.HypershuntScanner;

public class RelocationListener implements CurrentLocationChangedListener {

    private static final Logger log = Global.getLogger(scanthosegates.data.campaign.listeners.RelocationListener.class);
    static {log.setLevel(Level.ALL);}

    public void reportCurrentLocationChanged(LocationAPI previous, LocationAPI current) {
        for (SectorEntityToken cryo : current.getEntitiesWithTag(Tags.CRYOSLEEPER)) {
            CryosleeperScanner.tryCreateCryosleeperReportCustom(cryo, log, true, true);
        }
        for (SectorEntityToken shunt : current.getEntitiesWithTag(Tags.CORONAL_TAP)) {
            HypershuntScanner.tryCreateHypershuntReport(shunt, log, true, true);
        }
    }
}
