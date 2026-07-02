package com.aocc.majorproject

import com.aocc.framework.Input
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ButtonTest {

    private lateinit var menuButton: Button

    @Before
    fun setUp() {
        menuButton = Button(0, 0, 4, 0, "Menu")
    }

    @Test
    fun touchInBounds_returnsTrueInsideMenuButton() {
        val event = Input.TouchEvent()
        event.x = 100
        event.y = 50

        assertTrue(menuButton.touchInBounds(event))
    }

    @Test
    fun touchInBounds_returnsFalseOutsideMenuButton() {
        val event = Input.TouchEvent()
        event.x = 250
        event.y = 50

        assertFalse(menuButton.touchInBounds(event))
    }

    @Test
    fun touchInBounds_respectsMainMenuButtonDimensions() {
        val playButton = Button(420, 260, 1, 0, "Play")

        val inside = Input.TouchEvent()
        inside.x = 500
        inside.y = 300
        val outside = Input.TouchEvent()
        outside.x = 900
        outside.y = 300

        assertTrue(playButton.touchInBounds(inside))
        assertFalse(playButton.touchInBounds(outside))
    }
}
