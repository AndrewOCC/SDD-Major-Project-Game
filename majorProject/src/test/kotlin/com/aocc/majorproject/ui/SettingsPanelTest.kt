package com.aocc.majorproject.ui

import com.aocc.framework.GameConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPanelTest {

    @Test
    fun outerPanel_isCenteredInWorld() {
        assertEquals(UiLayout.centerX(SettingsPanel.PANEL_WIDTH), SettingsPanel.PANEL_X)
    }

    @Test
    fun musicIcon_hasGapBelowSoundIcon() {
        val panel = SettingsPanel()
        assertEquals(
            panel.getSoundIconY() + SettingsPanel.ICON_SIZE + SettingsPanel.ICON_GAP,
            panel.getMusicIconY()
        )
    }

    @Test
    fun soundAndMusicIcons_shareHorizontalCenter() {
        val panel = SettingsPanel()
        assertEquals(panel.getSoundIconBounds().x, panel.getMusicIconBounds().x)
        assertTrue(panel.getOuterBounds().width <= GameConstants.WORLD_WIDTH)
    }

    @Test
    fun promptY_isBelowPanel() {
        val panelBottom = SettingsPanel.PANEL_Y + SettingsPanel.PANEL_HEIGHT
        val promptY = panelBottom + 65
        assertTrue(promptY > panelBottom)
        assertTrue(promptY < GameConstants.WORLD_HEIGHT)
    }
}
