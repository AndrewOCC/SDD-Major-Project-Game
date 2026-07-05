package com.aocc.majorproject.ui

import com.aocc.framework.Input
import com.aocc.majorproject.GamePreferences
import com.aocc.majorproject.GameSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PauseMenuPanelTest {

    private lateinit var panel: PauseMenuPanel
    private lateinit var session: GameSession

    @Before
    fun setUp() {
        panel = PauseMenuPanel()
        session = GameSession()
    }

    @Test
    fun soundAndTiltColumns_shareItemSizeAndSpacingForSymmetry() {
        val sound = panel.getItemBounds(PauseMenuPanel.Item.SOUND)
        val music = panel.getItemBounds(PauseMenuPanel.Item.MUSIC)
        val secondScreen = panel.getItemBounds(PauseMenuPanel.Item.SECOND_SCREEN)

        val flat = panel.getItemBounds(PauseMenuPanel.Item.TILT_FLAT)
        val tilted = panel.getItemBounds(PauseMenuPanel.Item.TILT_TILTED)
        val custom = panel.getItemBounds(PauseMenuPanel.Item.TILT_CUSTOM)

        // Same row Y positions on both sides of the panel.
        assertEquals(sound.y, flat.y)
        assertEquals(music.y, tilted.y)
        assertEquals(secondScreen.y, custom.y)

        // Same vertical pitch between consecutive items on both sides.
        assertEquals(music.y - sound.y, tilted.y - flat.y)
        assertEquals(secondScreen.y - music.y, custom.y - tilted.y)
    }

    @Test
    fun allFocusItems_fitWithinTheOuterPanel() {
        val outer = panel.getOuterBounds()
        for (item in PauseMenuPanel.FOCUS_ITEMS) {
            val bounds = panel.getItemBounds(item)
            assertTrue("$item left edge inside panel", bounds.x >= outer.x)
            assertTrue("$item right edge inside panel", bounds.x + bounds.width <= outer.x + outer.width)
            assertTrue("$item top edge inside panel", bounds.y >= outer.y)
            assertTrue("$item bottom edge inside panel", bounds.y + bounds.height <= outer.y + outer.height)
        }
    }

    @Test
    fun focusItems_containsResumeAndQuit() {
        assertTrue(PauseMenuPanel.FOCUS_ITEMS.contains(PauseMenuPanel.Item.RESUME))
        assertTrue(PauseMenuPanel.FOCUS_ITEMS.contains(PauseMenuPanel.Item.QUIT))
        assertEquals(panel.getResumeBounds(), panel.getItemBounds(PauseMenuPanel.Item.RESUME))
        assertEquals(panel.getQuitBounds(), panel.getItemBounds(PauseMenuPanel.Item.QUIT))
    }

    @Test
    fun handleSettingsTouch_togglesSoundOnTapInsideBounds() {
        GamePreferences.sound = true
        val bounds = panel.getItemBounds(PauseMenuPanel.Item.SOUND)
        val event = touchUpAt(bounds.centerX(), bounds.centerY())

        val handled = panel.handleSettingsTouch(event, session.getPlayer())

        assertTrue(handled)
        assertFalse(GamePreferences.sound)
        GamePreferences.sound = true
    }

    @Test
    fun activateSettingsItem_appliesFlatTilt() {
        val player = session.getPlayer()

        panel.activateSettingsItem(PauseMenuPanel.Item.TILT_FLAT, player)

        assertEquals(1, player.tiltMode)
        assertEquals(0f, player.xBias, 0.001f)
        assertEquals(0f, player.yBias, 0.001f)
    }

    @Test
    fun activateSettingsItem_resumeAndQuitAreNoOps() {
        val player = session.getPlayer()
        val modeBefore = player.tiltMode

        panel.activateSettingsItem(PauseMenuPanel.Item.RESUME, player)
        panel.activateSettingsItem(PauseMenuPanel.Item.QUIT, player)

        assertEquals(modeBefore, player.tiltMode)
    }

    private fun touchUpAt(x: Int, y: Int): Input.TouchEvent {
        val event = Input.TouchEvent()
        event.type = Input.TouchEvent.TOUCH_UP
        event.x = x
        event.y = y
        return event
    }
}
