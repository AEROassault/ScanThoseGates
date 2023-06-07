package org.aero.scanThoseGates.campaign.intel.button;

import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public interface IntelButton {
    void buttonPressCancelled(IntelUIAPI ui);

    void buttonPressConfirmed(IntelUIAPI ui);

    void createConfirmationPrompt(TooltipMakerAPI tooltip);

    boolean doesButtonHaveConfirmDialog();

    String getName();

    int getShortcut();
}
