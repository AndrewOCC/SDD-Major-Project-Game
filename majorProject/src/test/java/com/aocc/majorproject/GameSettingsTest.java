package com.aocc.majorproject;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameSettingsTest {

    @Test
    public void toggleSound_flipsSoundFlag() {
        MainMenuScreen.sound = true;
        MainMenuScreen.tapVol = 10;

        GameSettings.toggleSound();
        assertFalse(MainMenuScreen.sound);
        assertTrue(MainMenuScreen.tapVol == 0);

        GameSettings.toggleSound();
        assertTrue(MainMenuScreen.sound);
        assertTrue(MainMenuScreen.tapVol == 10);
    }
}
