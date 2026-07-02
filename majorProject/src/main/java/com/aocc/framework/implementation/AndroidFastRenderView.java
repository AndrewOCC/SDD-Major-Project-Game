package com.aocc.framework.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Choreographer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.aocc.framework.GameConstants;
import com.aocc.framework.Viewport;

public class AndroidFastRenderView extends SurfaceView implements SurfaceHolder.Callback {
    private final AndroidGame game;
    private final Bitmap framebuffer;
    private final SurfaceHolder holder;
    private final Choreographer choreographer = Choreographer.getInstance();
    private final Choreographer.FrameCallback frameCallback = this::onFrame;

    private volatile boolean running;
    private long lastFrameTimeNanos;

    public AndroidFastRenderView(AndroidGame game, Bitmap framebuffer) {
        super(game);
        this.game = game;
        this.framebuffer = framebuffer;
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

        try {
            game.getCurrentScreen().update(deltaSeconds);
            game.getCurrentScreen().paint(deltaSeconds);
        } catch (RuntimeException e) {
            running = false;
            throw e;
        }

        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            try {
                Viewport viewport = game.getViewport();
                viewport.update(canvas.getWidth(), canvas.getHeight());
                game.updateInputViewport(viewport);

                canvas.drawColor(Color.BLACK);
                Rect destRect = viewport.getLetterboxDestRect();
                canvas.drawBitmap(framebuffer, null, destRect, null);
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
