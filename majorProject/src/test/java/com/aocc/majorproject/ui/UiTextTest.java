package com.aocc.majorproject.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UiTextTest {

    @Test
    public void uiBounds_centerMatchesRectangleMiddle() {
        UiBounds bounds = new UiBounds(540, 500, 200, 100);
        assertEquals(640, bounds.centerX());
        assertEquals(550, bounds.centerY());
    }

    @Test
    public void uiBounds_containsTouchInsideButton() {
        UiBounds bounds = new UiBounds(540, 500, 200, 100);
        assertTrue(bounds.contains(600f, 550f));
        assertFalse(bounds.contains(100f, 65f));
    }
}
