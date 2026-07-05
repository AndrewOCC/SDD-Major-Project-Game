package com.aocc.framework.implementation

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.aocc.framework.GameConstants
import com.aocc.framework.Viewport

/**
 * Game loop: update → paint into a letterbox-sized off-screen buffer → single
 * blit to the native surface. Avoids per-primitive surface scaling cost.
 */
class AndroidFastRenderView(
    private val game: AndroidGame,
    private val graphics: AndroidGraphics
) : SurfaceView(game), SurfaceHolder.Callback {

    private val holder: SurfaceHolder = getHolder()
    private val choreographer: Choreographer = Choreographer.getInstance()
    private val frameCallback: Choreographer.FrameCallback = Choreographer.FrameCallback { frameTimeNanos ->
        onFrame(frameTimeNanos)
    }
    private val blitPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    @Volatile
    private var running: Boolean = false
    private var lastFrameTimeNanos: Long = 0

    init {
        holder.addCallback(this)
    }

    fun resume() {
        running = true
        lastFrameTimeNanos = System.nanoTime()
        choreographer.postFrameCallback(frameCallback)
    }

    fun pause() {
        running = false
        choreographer.removeFrameCallback(frameCallback)
    }

    private fun onFrame(frameTimeNanos: Long) {
        if (!running) {
            return
        }

        if (!holder.surface.isValid) {
            choreographer.postFrameCallback(frameCallback)
            return
        }

        var deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
        lastFrameTimeNanos = frameTimeNanos
        if (deltaSeconds > GameConstants.MAX_DELTA_SECONDS) {
            deltaSeconds = GameConstants.MAX_DELTA_SECONDS
        }
        if (deltaSeconds < 0f) {
            deltaSeconds = 0f
        }

        game.currentScreen.update(deltaSeconds)

        val canvas = holder.lockCanvas()
        if (canvas != null) {
            try {
                val viewport = game.viewport
                viewport.update(canvas.width, canvas.height)
                game.updateInputViewport(viewport)

                if (graphics.beginFrame(viewport)) {
                    try {
                        game.currentScreen.paint(deltaSeconds)
                    } finally {
                        graphics.endFrame()
                    }
                }

                canvas.drawColor(Color.BLACK)
                val frameBuffer = graphics.frameBuffer
                if (frameBuffer != null) {
                    val dest = viewport.getLetterboxDestRect()
                    canvas.drawBitmap(frameBuffer, null, dest, blitPaint)
                }
            } catch (e: RuntimeException) {
                running = false
                throw e
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }

        choreographer.postFrameCallback(frameCallback)
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        if (running) {
            lastFrameTimeNanos = System.nanoTime()
            choreographer.postFrameCallback(frameCallback)
        }
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val viewport = game.viewport
        viewport.update(width, height)
        game.updateInputViewport(viewport)
        graphics.ensureFramebuffer(viewport)
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        choreographer.removeFrameCallback(frameCallback)
    }
}
