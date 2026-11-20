package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics
import com.aocc.framework.Screen
import com.aocc.majorproject.display.RearLayout
import com.aocc.majorproject.display.RearScreenUi
import com.aocc.majorproject.display.RearTapQueue
import com.aocc.majorproject.input.GamepadInput
import com.aocc.majorproject.ui.UiButton
import com.aocc.majorproject.ui.UiText

/** How-to-play art. In dual-screen mode its buttons move to the rear touch display. */
class TutorialScreen(private val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame), RearScreenUi {

    private val menuButton = UiButton.menuAt(0, 0)
    private val rearTaps = RearTapQueue()
    private val rearPlayButton = UiButton(50, 260, 620, 170, "Play", 72f)
    private val rearBackButton = UiButton(50, 460, 620, 120, "Back", 48f)

    override fun update(deltaTime: Float) {
        val topTouches = game.input.touchEvents
        val rearTouches = rearTaps.drain()
        if (majorProjectGame.isDualScreenActive()) {
            for (event in rearTouches) {
                if (rearPlayButton.touchInBounds(event)) {
                    playTap()
                    game.setScreen(GameScreen(majorProjectGame))
                    return
                }
                if (rearBackButton.touchInBounds(event)) {
                    goToMenu()
                    return
                }
            }
        } else {
            for (event in topTouches) {
                if (event.type == com.aocc.framework.Input.TouchEvent.TOUCH_UP && menuButton.touchInBounds(event)) {
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
        playTap()
        game.setScreen(MainMenuScreen(majorProjectGame))
    }

    private fun playTap() {
        Assets.tap?.play(GamePreferences.getTapVolume().toFloat())
    }

    override fun onRearTap(x: Int, y: Int) {
        rearTaps.add(x, y)
    }

    override fun paintRear(g: Graphics, paint: Paint) {
        paint.textSize = 54f
        UiText.drawCentered(g, paint, "How to Play", RearLayout.WIDTH / 2, 140, Color.WHITE)
        rearPlayButton.paint(g)
        rearBackButton.paint(g)
    }

    override fun paint(deltaTime: Float) {
        val g = game.graphics
        Assets.tutorial?.let { g.drawImage(it, 0, 0) }
        g.drawRect(0, 0, UiButton.MENU_WIDTH, LEGACY_MENU_ART_HEIGHT, Color.BLACK)
        if (!majorProjectGame.isDualScreenActive()) {
            menuButton.paint(g)
        }
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
