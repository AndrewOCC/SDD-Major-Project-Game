package com.aocc.majorproject

import com.aocc.framework.Graphics
import com.aocc.framework.Screen
import com.aocc.majorproject.input.GamepadInput
import com.aocc.majorproject.ui.MainMenuLayout
import com.aocc.majorproject.ui.SpatialFocusNavigator
import com.aocc.majorproject.ui.UiBounds
import com.aocc.majorproject.ui.UiSelectionHighlight

class MainMenuScreen(private val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame) {

    private var signInPressed = -1
    private var selectedMenuIndex = 0

    override fun update(deltaTime: Float) {
        handleGamepad(majorProjectGame.getGamepadInput().consumeActions())
        handleTouch(game.input.touchEvents)
    }

    private fun handleTouch(touchEvents: List<com.aocc.framework.Input.TouchEvent>) {
        for (i in touchEvents.indices) {
            val event = touchEvents[i]

            if (event.type == com.aocc.framework.Input.TouchEvent.TOUCH_UP) {
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

    private fun buildMenuFocusBounds(): List<UiBounds> {
        val loggedIn = majorProjectGame.isLoggedIn()
        return List(getMenuItemCount()) { i ->
            MainMenuLayout.highlightForIndex(i, !loggedIn)!!
        }
    }

    private fun getMenuItemCount(): Int {
        return if (majorProjectGame.isLoggedIn()) 4 else 5
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
        g.drawImage(Assets.menu_bg!!, 0, 0)

        g.drawImage(
            Assets.gpg_icon_leaderboards!!,
            MainMenuLayout.leaderboardsDrawX(), MainMenuLayout.leaderboardsDrawY()
        )
        g.drawImage(
            Assets.gpg_icon_achievements!!,
            MainMenuLayout.achievementsDrawX(), MainMenuLayout.achievementsDrawY()
        )

        if (!majorProjectGame.isLoggedIn()) {
            if (signInPressed >= 0) {
                g.drawImage(
                    Assets.sign_in_press!!,
                    MainMenuLayout.signInDrawX(), MainMenuLayout.signInDrawY()
                )
            } else {
                g.drawImage(
                    Assets.sign_in_base!!,
                    MainMenuLayout.signInDrawX(), MainMenuLayout.signInDrawY()
                )
            }
        }

        paintSelectionHighlight(g)
        VersionOverlay.paint(g)
    }

    private fun paintSelectionHighlight(g: Graphics) {
        val bounds = MainMenuLayout.highlightForIndex(
            selectedMenuIndex, !majorProjectGame.isLoggedIn()
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
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
