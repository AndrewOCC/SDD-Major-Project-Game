package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics
import com.aocc.framework.Screen
import com.aocc.majorproject.display.RearLayout
import com.aocc.majorproject.display.RearScreenUi
import com.aocc.majorproject.display.RearTapQueue
import com.aocc.majorproject.input.GamepadInput
import com.aocc.majorproject.roomba.MiteRenderer
import com.aocc.majorproject.roomba.RoombaRenderer
import com.aocc.majorproject.roomba.RoombaTheme
import com.aocc.majorproject.ui.UiButton
import com.aocc.majorproject.ui.UiText

/** How-to-play art. In dual-screen mode its buttons move to the rear touch display. */
class TutorialScreen(private val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame), RearScreenUi {

    private val menuButton = UiButton.menuAt(0, 0)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var time = 0f
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
        time += deltaTime
        val g = game.graphics
        Assets.floor?.let { g.drawImage(it, 0, 0) } ?: g.drawRect(0, 0, 1280, 720, Color.BLACK)
        g.drawARGB(150, 10, 10, 14)

        textPaint.typeface = Assets.plain
        textPaint.textSize = 50f
        UiText.drawCentered(g, textPaint, "How to Play", 640, 70, Color.WHITE)

        val iconX = 220f
        var y = 160
        for ((index, row) in ROWS.withIndex()) {
            paintIcon(g, index, iconX, y.toFloat())
            textPaint.textSize = 30f
            UiText.drawLeftOfCenter(g, textPaint, row.first, 300, y - 18, Color.WHITE)
            textPaint.textSize = 24f
            UiText.drawLeftOfCenter(g, textPaint, row.second, 300, y + 18, Color.LTGRAY)
            y += 115
        }

        if (!majorProjectGame.isDualScreenActive()) {
            menuButton.paint(g)
        }
        VersionOverlay.paint(g)
    }

    private fun paintIcon(g: Graphics, index: Int, x: Float, y: Float) {
        when (index) {
            0 -> RoombaRenderer.paint(g, x, y, 32f, -30f, time * 900f, 14f, 0f, 0f, false)
            1 -> MiteRenderer.paint(g, x, y, 24f, time, x + 60f, y, 1f)
            2 -> {
                g.drawCircle(x, y, 40f, RoombaTheme.DOCK_BASE)
                g.drawCircle(x, y, 34f, RoombaTheme.DOCK_PAD)
                for (side in listOf(-1, 1)) {
                    g.drawRect((x + side * 13 - 3).toInt(), (y - 11).toInt(), 6, 22, Color.rgb(210, 180, 90))
                }
                g.drawCircle(x, y + 20f, 4f, RoombaTheme.DOCK_GLOW)
            }
            else -> {
                for (i in 0 until 14) {
                    val a = i * 2.4
                    g.drawCircle(x + (kotlin.math.cos(a) * (8 + i * 2)).toFloat(),
                        y + (kotlin.math.sin(a) * (6 + i * 1.5)).toFloat(), 6f, Color.argb(150, 150, 140, 125))
                }
            }
        }
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
        private val ROWS = listOf(
            "You're the Roomba" to "Tilt to drive. Your blue ring is suction.",
            "Vacuum the dust mites" to "Each one grows your bin multiplier - while you still have suction.",
            "Dock to recharge suction" to "But linger and the dock empties your bin, resetting the multiplier.",
            "Clean the floor" to "Wipe away dust for bonuses at 25, 50, 75 and 95% clean.",
        )
    }
}
