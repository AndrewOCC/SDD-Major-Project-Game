package com.aocc.majorproject.ui;

import com.aocc.framework.GameConstants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SettingsPanelTest {

    @Test
    public void outerPanel_isCenteredInWorld() {
        assertEquals(UiLayout.centerX(SettingsPanel.PANEL_WIDTH), SettingsPanel.PANEL_X);
    }

    @Test
    public void musicIcon_hasGapBelowSoundIcon() {
        SettingsPanel panel = new SettingsPanel();
        assertEquals(panel.getSoundIconY() + SettingsPanel.ICON_SIZE + SettingsPanel.ICON_GAP,
                panel.getMusicIconY());
    }

    @Test
    public void soundAndMusicIcons_shareHorizontalCenter() {
        SettingsPanel panel = new SettingsPanel();
        assertEquals(panel.getSoundIconBounds().x, panel.getMusicIconBounds().x);
        assertTrue(panel.getOuterBounds().width <= GameConstants.WORLD_WIDTH);
    }
}
