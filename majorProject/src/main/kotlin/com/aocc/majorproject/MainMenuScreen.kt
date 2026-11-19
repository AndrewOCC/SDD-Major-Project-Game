package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics
import com.aocc.framework.Input.TouchEvent
import com.aocc.framework.Screen
import com.aocc.majorproject.display.RearLayout
import com.aocc.majorproject.display.RearScreenUi
import com.aocc.majorproject.display.RearTapQueue
import com.aocc.majorproject.input.GamepadInput
import com.aocc.majorproject.ui.LogoScene
import com.aocc.majorproject.ui.MainMenuLayout
import com.aocc.majorproject.ui.SpatialFocusNavigator
import com.aocc.majorproject.ui.UiBounds
import com.aocc.majorproject.ui.UiButton
import com.aocc.majorproject.ui.UiSelectionHighlight
import com.aocc.majorproject.ui.UiText

/**
 * Main menu. In dual-screen mode the menu itself lives on the rear touch display and the
 * top display shows the logo over drifting snow ([LogoScene]).
 */
class MainMenuScreen(private val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame), RearScreenUi {

    private var signInPressed = -1
    private var selectedMenuIndex = 0

    private val logoScene = LogoScene()
    private val rearTaps = RearTapQueue()
    private val rearPlayButton = UiButton(50, 70, 620, 170, "Play", 72f)
    private val rearTutorialButton = UiButton(50, 270, 620, 120, "How to Play", 48f)
    private val rearLeaderboardsTile = UiBounds(50, 420, 300, 170)
    private val rearAchievementsTile = UiBounds(370, 420, 300, 170)
    private val rearSignInTile = UiBounds(50, 620, 620, 130)

    override fun update(deltaTime: Float) {
        logoScene.update(deltaTime)
        val topTouches = game.input.touchEvents
        val rearTouches = rearTaps.drain()
        handleGamepad(majorProjectGame.getGamepadInput().consumeActions())
        // In dual-screen mode only the bottom touch screen has controls; top taps are ignored.
        if (isDual()) {
            handleRearTouch(rearTouches)
        } else {
            handleTouch(topTouches)
        }
    }

    private fun isDual(): Boolean = majorProjectGame.isDualScreenActive()

    private fun handleRearTouch(touchEvents: List<TouchEvent>) {
        for (event in touchEvents) {
            val index = buildMenuFocusBounds().indexOfFirst { it.contains(event) }
            if (index >= 0) {
                activateMenuItem(index)
                return
            }
        }
    }

    override fun onRearTap(x: Int, y: Int) {
        rearTaps.add(x, y)
    }

    override fun paintRear(g: Graphics, paint: Paint) {
        rearPlayButton.paint(g)
        rearTutorialButton.paint(g)
        paintRearTile(g, paint, rearLeaderboardsTile, Assets.gpg_icon_leaderboards, "Leaderboards")
        paintRearTile(g, paint, rearAchievementsTile, Assets.gpg_icon_achievements, "Achievements")
        if (!majorProjectGame.isLoggedIn()) {
            g.drawRect(rearSignInTile.x, rearSignInTile.y, rearSignInTile.width, rearSignInTile.height, Color.DKGRAY)
            Assets.sign_in_base?.let {
                g.drawImage(
                    it,
                    rearSignInTile.centerX() - it.width / 2,
                    rearSignInTile.centerY() - it.height / 2
                )
            }
        }
        buildMenuFocusBounds().getOrNull(selectedMenuIndex)?.let { UiSelectionHighlight.paintRect(g, it) }
    }

    private fun paintRearTile(g: Graphics, paint: Paint, tile: UiBounds, icon: com.aocc.framework.Image?, label: String) {
        g.drawRect(tile.x, tile.y, tile.width, tile.height, Color.DKGRAY)
        icon?.let { g.drawImage(it, tile.centerX() - it.width / 2, tile.y + 22) }
        paint.textSize = 32f
        UiText.drawCentered(g, paint, label, tile.centerX(), tile.y + tile.height - 34, Color.WHITE)
    }

    private fun handleTouch(touchEvents: List<TouchEvent>) {
        for (i in touchEvents.indices) {
            val event = touchEvents[i]

            if (event.type == TouchEvent.TOUCH_UP) {
                if (MainMenuLayout.isPlay(event)) {
                    startGame()
                }
                if (MainMenuLayout.isTutorial(event)) {
                    startTutorial()
                }
                if (!majorProjectGame.isLoggedIn() && MainMenuLayout.isSignIn(event)) {
                    playTap()
                    signInPressed = i
                    majorProjectGame.onSignInButtonClicked()
                }
                if (MainMenuLayout.isLeaderboards(event)) {
                    openLeaderboards()
                }
                if (MainMenuLayout.isAchievements(event)) {
                    openAchievements()
                }
            } else {
                signInPressed = -1
            }
        }
    }

    private fun handleGamepad(actions: List<GamepadInput.Action>) {
        val focusItems = buildMenuFocusBounds()
        for (action in actions) {
            val direction = SpatialFocusNavigator.directionFrom(action)
            if (direction != null) {
                selectedMenuIndex = SpatialFocusNavigator.findNext(
                    selectedMenuIndex, direction, focusItems
                )
                continue
            }
            if (action == GamepadInput.Action.CONFIRM) {
                activateMenuItem(selectedMenuIndex)
                continue
            }
            if (action == GamepadInput.Action.CANCEL) {
                backButton()
            }
        }
    }

    /** Same order as [activateMenuItem]: Play, Tutorial, [Sign in], Leaderboards, Achievements. */
    private fun buildMenuFocusBounds(): List<UiBounds> {
        val loggedIn = majorProjectGame.isLoggedIn()
        if (isDual()) {
            val items = arrayListOf(rearPlayButton.getBounds(), rearTutorialButton.getBounds())
            if (!loggedIn) {
                items.add(rearSignInTile)
            }
            items.add(rearLeaderboardsTile)
            items.add(rearAchievementsTile)
            return items
        }
        val items = ArrayList<UiBounds>(MainMenuLayout.menuItemCount(loggedIn))
        for (i in 0 until MainMenuLayout.menuItemCount(loggedIn)) {
            MainMenuLayout.highlightForIndex(i, loggedIn)?.let { items.add(it) }
        }
        return items
    }

    private fun activateMenuItem(index: Int) {
        if (!majorProjectGame.isLoggedIn()) {
            when (index) {
                0 -> startGame()
                1 -> startTutorial()
                2 -> {
                    playTap()
                    majorProjectGame.onSignInButtonClicked()
                }
                3 -> openLeaderboards()
                4 -> openAchievements()
            }
        } else {
            when (index) {
                0 -> startGame()
                1 -> startTutorial()
                2 -> openLeaderboards()
                3 -> openAchievements()
            }
        }
    }

    private fun startGame() {
        playTap()
        game.setScreen(GameScreen(majorProjectGame))
    }

    private fun startTutorial() {
        playTap()
        game.setScreen(TutorialScreen(majorProjectGame))
    }

    private fun openLeaderboards() {
        playTap()
        majorProjectGame.onShowLeaderboardsRequested("")
    }

    private fun openAchievements() {
        playTap()
        majorProjectGame.onShowAchievementsRequested("")
    }

    private fun playTap() {
        Assets.tap?.play(GamePreferences.getTapVolume().toFloat())
    }

    override fun paint(deltaTime: Float) {
        val g = game.graphics
        if (isDual()) {
            logoScene.paint(g)
            VersionOverlay.paint(g)
            return
        }
        Assets.menu_bg?.let { g.drawImage(it, 0, 0) }

        Assets.gpg_icon_leaderboards?.let {
            g.drawImage(it, MainMenuLayout.leaderboardsDrawX(), MainMenuLayout.leaderboardsDrawY())
        }
        Assets.gpg_icon_achievements?.let {
            g.drawImage(it, MainMenuLayout.achievementsDrawX(), MainMenuLayout.achievementsDrawY())
        }

        if (!majorProjectGame.isLoggedIn()) {
            val signInImage = if (signInPressed >= 0) Assets.sign_in_press else Assets.sign_in_base
            signInImage?.let {
                g.drawImage(it, MainMenuLayout.signInDrawX(), MainMenuLayout.signInDrawY())
            }
        }

        paintSelectionHighlight(g)
        VersionOverlay.paint(g)
    }

    private fun paintSelectionHighlight(g: Graphics) {
        val bounds = MainMenuLayout.highlightForIndex(
            selectedMenuIndex, majorProjectGame.isLoggedIn()
        ) ?: return
        UiSelectionHighlight.paintRect(g, bounds)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
    }

    override fun backButton() {
        majorProjectGame.finish()
    }
}
