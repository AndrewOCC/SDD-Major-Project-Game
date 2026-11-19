package com.aocc.majorproject.display

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import com.aocc.framework.Viewport
import com.aocc.framework.implementation.AndroidGraphics
import com.aocc.majorproject.Assets
import com.aocc.majorproject.MajorProjectGame
import com.aocc.majorproject.ui.ParticleField

/**
 * The rear display's touch surface in dual-screen mode. Draws the current screen's controls
 * (see [RearScreenUi]) with the game's own renderer, over drifting snow, in a portrait world
 * letterboxed onto the panel, and routes taps back to that screen.
 */
@SuppressLint("ViewConstructor")
class SecondaryControlsView(
    context: Context,
    private val activity: MajorProjectGame,
) : View(context) {

    private val graphics = AndroidGraphics(context.assets)
    private val viewport = Viewport(RearLayout.WIDTH, RearLayout.HEIGHT)
    private val particles = ParticleField(RearLayout.WIDTH, RearLayout.HEIGHT, count = 30, seed = 11L)
    private val blitPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 40f
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }
    private var lastFrameMillis = 0L

    private fun rearScreen(): RearScreenUi? = activity.currentScreen as? RearScreenUi

    override fun onDraw(canvas: Canvas) {
        // Keep redrawing so countdowns, toggles and the snow stay live.
        postInvalidateOnAnimation()
        val now = SystemClock.uptimeMillis()
        val delta = if (lastFrameMillis == 0L) 0f else ((now - lastFrameMillis) / 1000f).coerceAtMost(0.05f)
        lastFrameMillis = now

        canvas.drawColor(Color.BLACK)
        viewport.update(width, height)
        if (!graphics.beginFrame(viewport)) {
            return
        }
        graphics.drawARGB(255, 16, 16, 20)
        particles.update(delta)
        particles.paint(graphics)
        textPaint.typeface = Assets.plain
        rearScreen()?.paintRear(graphics, textPaint)
        graphics.endFrame()

        val frame = graphics.frameBuffer ?: return
        canvas.drawBitmap(frame, null, viewport.getLetterboxDestRect(), blitPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val screen = rearScreen() ?: return false
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            screen.onRearTap(viewport.screenToWorldX(event.x), viewport.screenToWorldY(event.y))
        }
        return true
    }
}
