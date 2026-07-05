package com.aocc.majorproject

import android.graphics.Color
import com.aocc.framework.Graphics
import com.aocc.framework.Screen
import com.aocc.majorproject.input.GamepadInput
import com.aocc.majorproject.ui.UiButton

class TutorialScreen(private val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame) {

    private val menuButton = UiButton.menuAt(0, 0)

    override fun update(deltaTime: Float) {
        val touchEvents = game.input.touchEvents
        for (event in touchEvents) {
            if (event.type == com.aocc.framework.Input.TouchEvent.TOUCH_UP) {
                if (menuButton.touchInBounds(event)) {
                    goToMenu()
                }
            }
        }

        for (action in majorProjectGame.getGamepadInput().consumeActions()) {
            if (action == GamepadInput.Action.CONFIRM || action == GamepadInput.Action.CANCEL) {
                goToMenu()
            }
        }
    }

    private fun goToMenu() {
        Assets.tap?.play(GamePreferences.getTapVolume().toFloat())
        game.setScreen(MainMenuScreen(majorProjectGame))
    }

    override fun paint(deltaTime: Float) {
        val g = game.graphics
        Assets.tutorial?.let { g.drawImage(it, 0, 0) }
        g.drawRect(0, 0, UiButton.MENU_WIDTH, LEGACY_MENU_ART_HEIGHT, Color.BLACK)
        menuButton.paint(g)
        VersionOverlay.paint(g)
    }

    override fun backButton() {
        game.setScreen(MainMenuScreen(majorProjectGame))
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
    }

    companion object {
        /** Legacy tutorial art includes a 200x100 menu button graphic beneath our control. */
        private const val LEGACY_MENU_ART_HEIGHT = 100
    }
}
