package com.aocc.majorproject.display

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import com.aocc.framework.Viewport
import com.aocc.framework.implementation.AndroidGraphics
import com.aocc.majorproject.Assets
import com.aocc.majorproject.GameScreen
import com.aocc.majorproject.MajorProjectGame

/**
 * Draws the game's own pause menu (same layout, art and font as the primary screen) on the
 * rear display, letterboxed from world coordinates, and routes taps back into [GameScreen].
 * Draws nothing unless the current screen is actually paused.
 */
@SuppressLint("ViewConstructor")
class SecondaryPauseView(
    context: Context,
    private val activity: MajorProjectGame,
) : View(context) {

    private val graphics = AndroidGraphics(context.assets)
    private val viewport = Viewport()
    private val blitPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 40f
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private fun pausedScreen(): GameScreen? {
        val screen = activity.currentScreen as? GameScreen ?: return null
        return if (screen.state == GameScreen.GameState.Paused) screen else null
    }

    override fun onDraw(canvas: Canvas) {
        if (visibility != VISIBLE) {
            return
        }
        // Keep redrawing while shown so the countdown / toggles stay live.
        postInvalidateOnAnimation()
        val screen = pausedScreen() ?: return

        viewport.update(width, height)
        if (!graphics.beginFrame(viewport)) {
            return
        }
        Assets.game_bg?.let { graphics.drawImage(it, 0, 0) }
        graphics.drawARGB(170, 0, 0, 0)
        textPaint.typeface = Assets.plain
        screen.paintPauseOverlay(graphics, textPaint)
        graphics.endFrame()

        val frame = graphics.frameBuffer ?: return
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(frame, null, viewport.getLetterboxDestRect(), blitPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val screen = pausedScreen() ?: return false
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            screen.enqueueSecondaryTap(
                viewport.screenToWorldX(event.x),
                viewport.screenToWorldY(event.y)
            )
        }
        return true
    }
}
