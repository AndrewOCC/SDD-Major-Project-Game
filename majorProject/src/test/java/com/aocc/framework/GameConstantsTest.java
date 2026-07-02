package com.aocc.framework;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GameConstantsTest {

    @Test
    public void secondsToSteps_oneSecondAtReferenceFpsEqualsSixtySteps() {
        assertEquals(60f, GameConstants.secondsToSteps(1f), 0.001f);
    }

    @Test
    public void framesToSeconds_convertsUsingReferenceFps() {
        assertEquals(500f / 60f, GameConstants.framesToSeconds(500f), 0.001f);
    }

    @Test
    public void worldDimensions_matchLegacyDesignResolution() {
        assertEquals(1280, GameConstants.WORLD_WIDTH);
        assertEquals(720, GameConstants.WORLD_HEIGHT);
    }
}
