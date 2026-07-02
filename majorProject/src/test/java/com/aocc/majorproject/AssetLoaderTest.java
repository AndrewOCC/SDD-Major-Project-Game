package com.aocc.majorproject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssetLoaderTest {

    @Test
    public void totalSteps_matchesLoadPipeline() {
        assertEquals(23, AssetLoader.TOTAL_STEP_COUNT);
    }
}
