package com.aocc.majorproject.ui

import com.aocc.framework.Input
import com.aocc.framework.PersonalMethods

/** Main menu hit regions anchored to the world rectangle (works with viewport letterboxing). */
object MainMenuLayout {

    const val BUTTON_WIDTH = 440
    const val BUTTON_HEIGHT = 200
    const val BUTTON_Y = 435
    const val BUTTON_GAP = 80

    const val GPG_BUTTON_WIDTH = 100
    const val GPG_BUTTON_HEIGHT = 80
    const val GPG_MARGIN = 5

    const val SIGN_IN_WIDTH = 180
    const val SIGN_IN_HEIGHT = 60
    const val SIGN_IN_MARGIN = 7

    /** Inset from touch targets to match visible art on menu_bg for Play/Tutorial. */
    private const val PLAY_HIGHLIGHT_INSET_X = 26
    private const val PLAY_HIGHLIGHT_INSET_Y = 38

    @JvmStatic
    fun playButton(): UiBounds {
        val rowWidth = BUTTON_WIDTH * 2 + BUTTON_GAP
        val rowX = UiLayout.centerX(rowWidth)
        return UiBounds(rowX, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
    }

    @JvmStatic
    fun tutorialButton(): UiBounds {
        val play = playButton()
        return UiBounds(play.x + BUTTON_WIDTH + BUTTON_GAP, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
    }

    @JvmStatic
    fun leaderboardsButton(): UiBounds {
        return UiBounds(UiLayout.alignRight(GPG_BUTTON_WIDTH, GPG_MARGIN), GPG_MARGIN,
            GPG_BUTTON_WIDTH, GPG_BUTTON_HEIGHT)
    }

    @JvmStatic
    fun achievementsButton(): UiBounds {
        val leaderboards = leaderboardsButton()
        return UiBounds(leaderboards.x, leaderboards.y + GPG_BUTTON_HEIGHT,
            GPG_BUTTON_WIDTH, GPG_BUTTON_HEIGHT)
    }

    @JvmStatic
    fun signInButton(): UiBounds {
        return UiBounds(SIGN_IN_MARGIN, SIGN_IN_MARGIN, SIGN_IN_WIDTH, SIGN_IN_HEIGHT)
    }

    /** Number of focusable menu items for the given sign-in state. */
    @JvmStatic
    fun menuItemCount(loggedIn: Boolean): Int = if (loggedIn) 4 else 5

    /**
     * Focus ring bounds aligned to visible menu art (not the full touch target).
     * Signed-out menus include the sign-in button at index 2.
     */
    @JvmStatic
    fun highlightForIndex(index: Int, loggedIn: Boolean): UiBounds? {
        if (loggedIn) {
            return when (index) {
                0 -> playButton().inset(PLAY_HIGHLIGHT_INSET_X, PLAY_HIGHLIGHT_INSET_Y)
                1 -> tutorialButton().inset(PLAY_HIGHLIGHT_INSET_X, PLAY_HIGHLIGHT_INSET_Y)
                2 -> leaderboardsButton()
                3 -> achievementsButton()
                else -> null
            }
        }
        return when (index) {
            0 -> playButton().inset(PLAY_HIGHLIGHT_INSET_X, PLAY_HIGHLIGHT_INSET_Y)
            1 -> tutorialButton().inset(PLAY_HIGHLIGHT_INSET_X, PLAY_HIGHLIGHT_INSET_Y)
            2 -> signInButton()
            3 -> leaderboardsButton()
            4 -> achievementsButton()
            else -> null
        }
    }

    @JvmStatic
    fun isPlay(event: Input.TouchEvent): Boolean = playButton().contains(event)

    @JvmStatic
    fun isTutorial(event: Input.TouchEvent): Boolean = tutorialButton().contains(event)

    @JvmStatic
    fun isLeaderboards(event: Input.TouchEvent): Boolean = leaderboardsButton().contains(event)

    @JvmStatic
    fun isAchievements(event: Input.TouchEvent): Boolean = achievementsButton().contains(event)

    @JvmStatic
    fun isSignIn(event: Input.TouchEvent): Boolean = signInButton().contains(event)

    @JvmStatic
    fun leaderboardsDrawX(): Int = leaderboardsButton().x

    @JvmStatic
    fun leaderboardsDrawY(): Int = leaderboardsButton().y

    @JvmStatic
    fun achievementsDrawX(): Int = achievementsButton().x

    @JvmStatic
    fun achievementsDrawY(): Int = achievementsButton().y

    @JvmStatic
    fun signInDrawX(): Int = signInButton().x

    @JvmStatic
    fun signInDrawY(): Int = signInButton().y

    @JvmStatic
    fun touchInBounds(event: Input.TouchEvent, bounds: UiBounds): Boolean {
        return PersonalMethods.touchInBounds(event, bounds.x, bounds.y, bounds.width, bounds.height)
    }
}
