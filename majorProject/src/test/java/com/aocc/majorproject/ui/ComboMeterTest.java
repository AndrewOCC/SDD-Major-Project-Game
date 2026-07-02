package com.aocc.majorproject.ui;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ComboMeterTest {

    @Test
    public void comboMeter_sitsLeftOfScoreBar() {
        ComboMeter comboMeter = new ComboMeter();
        assertTrue(comboMeter.getBounds().x + comboMeter.getBounds().width < ScoreBar.BAR_X);
    }
}
