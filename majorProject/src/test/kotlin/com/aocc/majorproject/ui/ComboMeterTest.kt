package com.aocc.majorproject.ui

import org.junit.Assert.assertTrue
import org.junit.Test

class ComboMeterTest {

    @Test
    fun comboMeter_sitsLeftOfScoreBar() {
        val comboMeter = ComboMeter()
        assertTrue(comboMeter.getBounds().x + comboMeter.getBounds().width < ScoreBar.BAR_X)
    }
}
