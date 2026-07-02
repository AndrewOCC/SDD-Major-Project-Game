package com.aocc.framework.implementation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.aocc.framework.GameConstants;
import com.aocc.framework.Viewport;

/**
 * Game loop: update → paint into a letterbox-sized off-screen buffer → single
 * blit to the native surface. Avoids per-primitive surface scaling cost.
 */
public class AndroidFastRenderView extends SurfaceView implements SurfaceHolder.Callback {
    private final AndroidGame game;
    private final AndroidGraphics graphics;
    private final SurfaceHolder holder;
    private final Choreographer choreographer = Choreographer.getInstance();
    private final Choreographer.FrameCallback frameCallback = this::onFrame;
    private final Paint blitPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private volatile boolean running;
    private long lastFrameTimeNanos;

    public AndroidFastRenderView(AndroidGame game, AndroidGraphics graphics) {
        super(game);
        this.game = game;
        this.graphics = graphics;
        this.holder = getHolder();
        holder.addCallback(this);
    }

    public void resume() {
        running = true;
        lastFrameTimeNanos = System.nanoTime();
        choreographer.postFrameCallback(frameCallback);
    }

    public void pause() {
        running = false;
        choreographer.removeFrameCallback(frameCallback);
    }

    private void onFrame(long frameTimeNanos) {
        if (!running) {
            return;
        }

        if (!holder.getSurface().isValid()) {
            choreographer.postFrameCallback(frameCallback);
            return;
        }

        float deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f;
        lastFrameTimeNanos = frameTimeNanos;
        if (deltaSeconds > GameConstants.MAX_DELTA_SECONDS) {
            deltaSeconds = GameConstants.MAX_DELTA_SECONDS;
        }
        if (deltaSeconds < 0f) {
            deltaSeconds = 0f;
        }

        game.getCurrentScreen().update(deltaSeconds);

        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            try {
                Viewport viewport = game.getViewport();
                viewport.update(canvas.getWidth(), canvas.getHeight());
                game.updateInputViewport(viewport);

                graphics.beginFrame(viewport);
                try {
                    game.getCurrentScreen().paint(deltaSeconds);
                } finally {
                    graphics.endFrame();
                }

                canvas.drawColor(Color.BLACK);
                if (graphics.getFrameBuffer() != null) {
                    Rect dest = viewport.getLetterboxDestRect();
                    canvas.drawBitmap(graphics.getFrameBuffer(), null, dest, blitPaint);
                }
            } catch (RuntimeException e) {
                running = false;
                throw e;
            } finally {
                holder.unlockCanvasAndPost(canvas);
            }
        }

        choreographer.postFrameCallback(frameCallback);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (running) {
            lastFrameTimeNanos = System.nanoTime();
            choreographer.postFrameCallback(frameCallback);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Viewport viewport = game.getViewport();
        viewport.update(width, height);
        game.updateInputViewport(viewport);
        graphics.ensureFramebuffer(viewport);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        choreographer.removeFrameCallback(frameCallback);
    }
}
