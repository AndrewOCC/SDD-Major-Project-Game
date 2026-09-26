package com.aocc.majorproject.ui

import com.aocc.framework.GameConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MainMenuLayoutTest {

    @Test
    fun playAndTutorialButtons_areCenteredInWorld() {
        val play = MainMenuLayout.playButton()
        val tutorial = MainMenuLayout.tutorialButton()

        val rowWidth = MainMenuLayout.BUTTON_WIDTH * 2 + MainMenuLayout.BUTTON_GAP
        assertEquals(UiLayout.centerX(rowWidth), play.x)
        assertEquals(play.x + MainMenuLayout.BUTTON_WIDTH + MainMenuLayout.BUTTON_GAP, tutorial.x)
        assertEquals(MainMenuLayout.BUTTON_Y, play.y)
        assertEquals(MainMenuLayout.BUTTON_Y, tutorial.y)
    }

    @Test
    fun gpgButtons_alignToRightEdge() {
        val leaderboards = MainMenuLayout.leaderboardsButton()
        assertEquals(
            GameConstants.WORLD_WIDTH - MainMenuLayout.GPG_BUTTON_WIDTH - MainMenuLayout.GPG_MARGIN,
            leaderboards.x
        )
    }

    @Test
    fun playHighlight_isInsetFromTouchTarget() {
        val touch = MainMenuLayout.playButton()
        val highlight = MainMenuLayout.highlightForIndex(0, true)!!
        assertTrue(highlight.width < touch.width)
        assertTrue(highlight.height < touch.height)
    }

    @Test
    fun playButton_matchesMeasuredMenuArt() {
        val play = MainMenuLayout.playButton()
        // Measured from menu-bg.png: Play art x[138,575] y[433,632].
        assertEquals(138, play.x)
        assertEquals(MainMenuLayout.BUTTON_Y, play.y)
        assertEquals(437, play.width)
        assertEquals(199, play.height)
    }

    @Test
    fun tutorialButton_mirrorsPlayAcrossCentre() {
        val play = MainMenuLayout.playButton()
        val tutorial = MainMenuLayout.tutorialButton()
        val leftGap = play.x
        val rightGap = GameConstants.WORLD_WIDTH - (tutorial.x + tutorial.width)
        assertEquals(leftGap, rightGap)
    }
}
