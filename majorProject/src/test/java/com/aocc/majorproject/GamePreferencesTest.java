package com.aocc.majorproject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GamePreferencesTest {

    @Test
    public void getTapVolume_reflectsSoundFlag() {
        GamePreferences.sound = true;
        assertEquals(10, GamePreferences.getTapVolume());

        GamePreferences.sound = false;
        assertEquals(0, GamePreferences.getTapVolume());
    }

    @Test
    public void setters_updateInMemoryValues() {
        GamePreferences.setSound(false);
        assertFalse(GamePreferences.sound);

        GamePreferences.setMusic(false);
        assertFalse(GamePreferences.music);

        GamePreferences.setTiltMode(1);
        assertEquals(1, GamePreferences.tiltMode);

        GamePreferences.setSecondScreenEnabled(true);
        assertTrue(GamePreferences.secondScreenEnabled);
    }
}
