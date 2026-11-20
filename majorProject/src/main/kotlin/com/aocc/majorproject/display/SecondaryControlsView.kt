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
 * that fills the panel edge to edge, and routes taps back to that screen.
 */
@SuppressLint("ViewConstructor")
class SecondaryControlsView(
    context: Context,
    private val activity: MajorProjectGame,
) : View(context) {

    private val graphics = AndroidGraphics(context.assets)

    // The world is sized to the panel's own shape so the background fills it edge to edge;
    // the RearLayout design box is centred inside at the largest scale that fits.
    private var viewport = Viewport(RearLayout.WIDTH, RearLayout.HEIGHT)
    private var particles = ParticleField(RearLayout.WIDTH, RearLayout.HEIGHT, count = 30, seed = 11L)
    private var contentOffsetX = 0f
    private var contentOffsetY = 0f
    private var laidOutWidth = 0
    private var laidOutHeight = 0
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
        layoutWorld()
        if (!graphics.beginFrame(viewport)) {
            return
        }
        graphics.drawARGB(255, 16, 16, 20)
        particles.update(delta)
        particles.paint(graphics)
        textPaint.typeface = Assets.plain
        graphics.pushOffset(contentOffsetX, contentOffsetY)
        rearScreen()?.paintRear(graphics, textPaint)
        graphics.popOffset()
        graphics.endFrame()

        val frame = graphics.frameBuffer ?: return
        canvas.drawBitmap(frame, null, viewport.getLetterboxDestRect(), blitPaint)
    }

    private fun layoutWorld() {
        if (width <= 0 || height <= 0 || (width == laidOutWidth && height == laidOutHeight)) {
            return
        }
        laidOutWidth = width
        laidOutHeight = height
        val scale = minOf(width.toFloat() / RearLayout.WIDTH, height.toFloat() / RearLayout.HEIGHT)
        val worldWidth = Math.round(width / scale)
        val worldHeight = Math.round(height / scale)
        viewport = Viewport(worldWidth, worldHeight).apply { update(width, height) }
        particles = ParticleField(worldWidth, worldHeight, count = 30, seed = 11L)
        contentOffsetX = (worldWidth - RearLayout.WIDTH) / 2f
        contentOffsetY = (worldHeight - RearLayout.HEIGHT) / 2f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val screen = rearScreen() ?: return false
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            screen.onRearTap(
                Math.round(viewport.screenToWorldX(event.x) - contentOffsetX),
                Math.round(viewport.screenToWorldY(event.y) - contentOffsetY)
            )
        }
        return true
    }
}
