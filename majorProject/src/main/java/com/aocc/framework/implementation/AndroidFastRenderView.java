package com.aocc.framework.implementation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.aocc.framework.GameConstants;
import com.aocc.framework.Viewport;

/**
 * Renders each frame directly to the device surface at native resolution.
 * Game logic and layout use virtual world coordinates; the viewport transform
 * scales them to the letterboxed on-screen area.
 */
public class AndroidFastRenderView extends SurfaceView implements SurfaceHolder.Callback {
    private final AndroidGame game;
    private final AndroidGraphics graphics;
    private final SurfaceHolder holder;
    private final Choreographer choreographer = Choreographer.getInstance();
    private final Choreographer.FrameCallback frameCallback = this::onFrame;

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

        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            try {
                Viewport viewport = game.getViewport();
                viewport.update(canvas.getWidth(), canvas.getHeight());
                game.updateInputViewport(viewport);

                game.getCurrentScreen().update(deltaSeconds);

                canvas.drawColor(Color.BLACK);
                graphics.beginFrame(canvas, viewport);
                try {
                    game.getCurrentScreen().paint(deltaSeconds);
                } finally {
                    graphics.endFrame();
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
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        choreographer.removeFrameCallback(frameCallback);
    }
}
