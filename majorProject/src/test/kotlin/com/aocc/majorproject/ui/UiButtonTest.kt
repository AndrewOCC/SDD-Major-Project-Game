package com.aocc.majorproject.ui

import com.aocc.framework.Input
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UiButtonTest {

    private lateinit var retryButton: UiButton

    @Before
    fun setUp() {
        retryButton = UiButton(540, 500, UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT, "Retry")
    }

    @Test
    fun touchInBounds_returnsTrueInsideButton() {
        val event = Input.TouchEvent()
        event.x = 640
        event.y = 550
        assertTrue(retryButton.touchInBounds(event))
    }

    @Test
    fun menuAt_usesStandardMenuDimensions() {
        val menu = UiButton.menuAt(0, 0)
        assertEquals(UiButton.MENU_WIDTH, menu.getBounds().width)
        assertEquals(UiButton.MENU_HEIGHT, menu.getBounds().height)
    }

    @Test
    fun touchInBounds_returnsFalseOutsideButton() {
        val event = Input.TouchEvent()
        event.x = 100
        event.y = 65
        assertFalse(retryButton.touchInBounds(event))
    }
}
