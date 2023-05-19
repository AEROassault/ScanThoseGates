package scanthosegates.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.DiscoverEntityListener;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import scanthosegates.campaign.econ.abilities.CryosleeperScanner;
import scanthosegates.campaign.econ.abilities.HypershuntScanner;

public class SalvagingListener implements DiscoverEntityListener {
    private static final Logger log = Global.getLogger(SalvagingListener.class);
    static {log.setLevel(Level.ALL);}

    @Override
    public void reportEntityDiscovered(SectorEntityToken entity){
        CryosleeperScanner.tryCreateCryosleeperReportCustom(entity, log, true, true);
        HypershuntScanner.tryCreateHypershuntReport(entity, log, true, true);
    }
}
