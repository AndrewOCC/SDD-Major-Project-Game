package com.aocc.majorproject.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UiTextTest {

    @Test
    fun uiBounds_centerMatchesRectangleMiddle() {
        val bounds = UiBounds(540, 500, 200, 100)
        assertEquals(640, bounds.centerX())
        assertEquals(550, bounds.centerY())
    }

    @Test
    fun uiBounds_containsTouchInsideButton() {
        val bounds = UiBounds(540, 500, 200, 100)
        assertTrue(bounds.contains(600f, 550f))
        assertFalse(bounds.contains(100f, 65f))
    }
}
