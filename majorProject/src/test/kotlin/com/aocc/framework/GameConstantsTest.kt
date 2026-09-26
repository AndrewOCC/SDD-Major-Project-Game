package com.aocc.framework

import org.junit.Assert.assertEquals
import org.junit.Test

class GameConstantsTest {

    @Test
    fun secondsToSteps_oneSecondAtReferenceFpsEqualsSixtySteps() {
        assertEquals(60f, GameConstants.secondsToSteps(1f), 0.001f)
    }

    @Test
    fun framesToSeconds_convertsUsingReferenceFps() {
        assertEquals(500f / 60f, GameConstants.framesToSeconds(500f), 0.001f)
    }

    @Test
    fun worldDimensions_matchLegacyDesignResolution() {
        assertEquals(1280, GameConstants.WORLD_WIDTH)
        assertEquals(720, GameConstants.WORLD_HEIGHT)
    }
}
