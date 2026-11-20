package com.aocc.majorproject

import org.junit.Assert.assertEquals
import org.junit.Test

class AssetLoaderTest {

    @Test
    fun totalSteps_matchesLoadPipeline() {
        assertEquals(26, AssetLoader.TOTAL_STEP_COUNT)
    }
}
