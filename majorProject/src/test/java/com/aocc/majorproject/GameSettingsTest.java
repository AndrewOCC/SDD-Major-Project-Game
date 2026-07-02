package com.aocc.majorproject;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameSettingsTest {

    @Test
    public void toggleSound_flipsSoundFlag() {
        GamePreferences.sound = true;

        GameSettings.toggleSound();
        assertFalse(GamePreferences.sound);
        assertTrue(GamePreferences.getTapVolume() == 0);

        GameSettings.toggleSound();
        assertTrue(GamePreferences.sound);
        assertTrue(GamePreferences.getTapVolume() == 10);
    }
}
