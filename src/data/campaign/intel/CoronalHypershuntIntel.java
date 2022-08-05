package data.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.intel.button.LayInCourse;
import data.scripts.stg_ModPlugin;

import java.awt.*;
import java.util.Set;

public class CoronalHypershuntIntel extends BaseIntel {
    public static final String INTEL_HYPERSHUNT = stg_ModPlugin.INTEL_MEGASTRUCTURES;
    private final SectorEntityToken hypershunt;

    public CoronalHypershuntIntel(SectorEntityToken hypershunt) {
        this.hypershunt = hypershunt;
    }

    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);

        float initPad;
        if (mode == ListInfoMode.IN_DESC) {
            initPad = 10f;
        } else {
            initPad = 3f;
        }

        bullet(info);
        info.addPara(hypershunt.getStarSystem().getName(), initPad, getBulletColorForMode(mode));
        unindent(info);
    }

    public String getSmallDescriptionTitle() {
        return "Coronal Hypershunt";
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float opad = 10f;

        Description desc = Global.getSettings().getDescription("coronal_tap", Description.Type.CUSTOM);

        TooltipMakerAPI text = info.beginImageWithText(hypershunt.getCustomEntitySpec().getSpriteName(), 64);
        text.addPara(desc.getText1FirstPara(), Misc.getGrayColor(), opad);
        info.addImageWithText(opad);

        info.addPara(
                "Located in the " + hypershunt.getStarSystem().getNameWithLowercaseType() + ".",
                opad,
                Misc.getPositiveHighlightColor(),
                hypershunt.getStarSystem().getBaseName()
        );

        addGenericButton(info, width, new LayInCourse(hypershunt));
    }

    @Override
    public String getIcon() {
        return hypershunt.getCustomEntitySpec().getIconName();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(INTEL_HYPERSHUNT);

        return tags;
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return super.getFactionForUIColors();
    }

    @Override
    protected String getName() {
        return "Coronal Hypershunt Location";
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return getEntity();
    }

    @Override
    public boolean shouldRemoveIntel() {
        return hypershunt == null || !hypershunt.isAlive();
    }

    @Override
    public String getCommMessageSound() {
        return "ui_discovered_entity";
    }

    @Override
    public SectorEntityToken getEntity() {
        return hypershunt;
    }

    @Override
    public IntelSortTier getSortTier() {
        return IntelSortTier.TIER_6;
    }
}
