package com.aocc.majorproject

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSettingsTest {

    @Test
    fun toggleSound_flipsSoundFlag() {
        GamePreferences.sound = true

        GameSettings.toggleSound()
        assertFalse(GamePreferences.sound)
        assertTrue(GamePreferences.getTapVolume() == 0)

        GameSettings.toggleSound()
        assertTrue(GamePreferences.sound)
        assertTrue(GamePreferences.getTapVolume() == 10)
    }
}
