package org.aero.scanThoseGates.campaign.intel

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.IntelSortTier
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.aero.scanThoseGates.ModPlugin.Settings.INTEL_MEGASTRUCTURES
import org.aero.scanThoseGates.campaign.intel.button.LayInCourse

class CryosleeperIntel(private val cryosleeper: SectorEntityToken) : BaseIntel() {
    override fun createIntelInfo(info: TooltipMakerAPI, mode: ListInfoMode) {
        val c = getTitleColor(mode)
        info.addPara(name, c, 0f)
        val initPad: Float
        initPad = if (mode == ListInfoMode.IN_DESC) {
            10f
        } else {
            3f
        }
        bullet(info)
        info.addPara(cryosleeper.starSystem.name, initPad, getBulletColorForMode(mode))
        unindent(info)
    }

    override fun getSmallDescriptionTitle(): String {
        return "Cryosleeper"
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val opad = 10f
        val desc = Global.getSettings().getDescription("derelict_cryosleeper", Description.Type.CUSTOM)
        val text = info.beginImageWithText(cryosleeper.customEntitySpec.spriteName, 64f)
        text.addPara(desc.text1FirstPara, Misc.getGrayColor(), opad)
        info.addImageWithText(opad)
        info.addPara(
            "Located in the " + cryosleeper.starSystem.nameWithLowercaseType + ".",
            opad,
            Misc.getPositiveHighlightColor(),
            cryosleeper.starSystem.baseName
        )
        addGenericButton(info, width, LayInCourse(cryosleeper))
    }

    override fun getIcon(): String {
        return cryosleeper.customEntitySpec.iconName
    }

    override fun getIntelTags(map: SectorMapAPI): Set<String> {
        val tags = super.getIntelTags(map)
        tags.add(INTEL_CRYOSLEEPER)
        return tags
    }

    override fun getFactionForUIColors(): FactionAPI {
        return super.getFactionForUIColors()
    }

    override fun getName(): String {
        return "Cryosleeper Location"
    }

    override fun getMapLocation(map: SectorMapAPI): SectorEntityToken {
        return entity
    }

    override fun shouldRemoveIntel(): Boolean {
        return !cryosleeper.isAlive
    }

    override fun getCommMessageSound(): String {
        return "ui_discovered_entity"
    }

    override fun getEntity(): SectorEntityToken {
        return cryosleeper
    }

    override fun getSortTier(): IntelSortTier {
        return IntelSortTier.TIER_6
    }

    companion object {
        const val INTEL_CRYOSLEEPER = INTEL_MEGASTRUCTURES
    }
}
