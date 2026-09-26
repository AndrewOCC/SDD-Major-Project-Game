package com.aocc.majorproject

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePreferencesTest {

    @Test
    fun getTapVolume_reflectsSoundFlag() {
        GamePreferences.sound = true
        assertEquals(10, GamePreferences.getTapVolume())

        GamePreferences.sound = false
        assertEquals(0, GamePreferences.getTapVolume())
    }

    @Test
    fun setters_updateInMemoryValues() {
        GamePreferences.setSound(false)
        assertFalse(GamePreferences.sound)

        GamePreferences.setMusic(false)
        assertFalse(GamePreferences.music)

        GamePreferences.setTiltMode(1)
        assertEquals(1, GamePreferences.tiltMode)

        GamePreferences.setSecondScreenEnabled(true)
        assertTrue(GamePreferences.secondScreenEnabled)
    }
}
