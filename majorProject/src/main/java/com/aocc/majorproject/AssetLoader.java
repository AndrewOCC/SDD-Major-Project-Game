package com.aocc.majorproject;

import android.app.Activity;
import android.graphics.Typeface;

import com.aocc.framework.Audio;
import com.aocc.framework.Graphics;
import com.aocc.framework.Graphics.ImageFormat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loads game assets on a background thread and reports progress to the loading screen.
 */
public final class AssetLoader {

    public interface Listener {
        void onProgress(int loaded, int total);

        void onComplete();

        void onError(Exception error);
    }

    public static final int TOTAL_STEP_COUNT = 23;

    private static final int TOTAL_STEPS = TOTAL_STEP_COUNT;

    private final Activity activity;
    private final MajorProjectGame game;
    private final Graphics graphics;
    private final Audio audio;
    private final Listener listener;
    private final int pixelScale;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicInteger loadedSteps = new AtomicInteger();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);

    public AssetLoader(MajorProjectGame game, Listener listener) {
        this.activity = game;
        this.game = game;
        this.graphics = game.getGraphics();
        this.audio = game.getAudio();
        this.listener = listener;
        this.pixelScale = AssetScale.tierFromViewport(game.getViewport().getScale());
        Assets.loadedPixelScale = this.pixelScale;
    }

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        executor.execute(() -> {
            try {
                loadSplash();
                loadImages();
                loadSounds();
                loadFonts();

                activity.runOnUiThread(() -> {
                    try {
                        Assets.loadMusic(game);
                        stepComplete();
                        markComplete();
                    } catch (RuntimeException e) {
                        listener.onError(e);
                        shutdown();
                    }
                });
            } catch (RuntimeException e) {
                activity.runOnUiThread(() -> {
                    listener.onError(e);
                    shutdown();
                });
            }
        });
    }

    public boolean isFinished() {
        return finished.get();
    }

    public float getProgress() {
        return loadedSteps.get() / (float) TOTAL_STEPS;
    }

    public int getLoadedSteps() {
        return loadedSteps.get();
    }

    public int getTotalSteps() {
        return TOTAL_STEPS;
    }

    public int getPixelScale() {
        return pixelScale;
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private void loadSplash() {
        Assets.splash = loadImage("splash.png", ImageFormat.RGB565, false);
        stepComplete();
    }

    private void loadImages() {
        Assets.noise = loadImage("noise.png", ImageFormat.ARGB4444, false);
        stepComplete();
        Assets.menu_bg = loadImage("menu-bg.png", ImageFormat.RGB565, false);
        stepComplete();
        Assets.game_bg = loadImage("game-bg.png", ImageFormat.RGB565, false);
        stepComplete();
        Assets.sign_in_base = loadImage("Red-signin_Medium_base.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.sign_in_press = loadImage("Red-signin_Medium_press.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.gpg_icon_leaderboards = loadImage("gpg-icon-leaderboards.png", ImageFormat.RGB565, true);
        stepComplete();
        Assets.gpg_icon_achievements = loadImage("gpg-icon-achievements.png", ImageFormat.RGB565, true);
        stepComplete();
        Assets.tilt_control_flat = loadImage("tilt-button-flat.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.tilt_control_tilted = loadImage("tilt-button-tilted.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.tilt_control_custom = loadImage("tilt-button-custom.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.tilt_control_flat_2 = loadImage("tilt-button-flat-2.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.tilt_control_tilted_2 = loadImage("tilt-button-tilted-2.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.tilt_control_custom_2 = loadImage("tilt-button-custom-2.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.sound = loadImage("sound.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.sound_muted = loadImage("sound-muted.png", ImageFormat.ARGB4444, true);
        Assets.music = loadImage("music.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.music_muted = loadImage("music-muted.png", ImageFormat.ARGB4444, true);
        stepComplete();
        Assets.tutorial = loadImage("tutorial.png", ImageFormat.RGB565, false);
        stepComplete();
    }

    /** Full-screen backgrounds stay at 1×; UI sprites may use 2×. */
    private com.aocc.framework.Image loadImage(String path, ImageFormat format, boolean allow2x) {
        int scale = allow2x ? pixelScale : 1;
        return graphics.newImage(path, format, scale);
    }

    private void loadSounds() {
        Assets.tap = audio.createSound("tap.wav");
        stepComplete();
        Assets.zap = audio.createSound("zap.wav");
        stepComplete();
        Assets.burn = audio.createSound("burn.wav");
        stepComplete();
        Assets.powerup = audio.createSound("powerup.wav");
        stepComplete();
    }

    private void loadFonts() {
        Assets.plain = Typeface.createFromAsset(game.getAssets(), "fonts/power_clear.ttf");
        Assets.bold = Typeface.create(Assets.plain, Typeface.BOLD);
        stepComplete();
    }

    private void stepComplete() {
        int loaded = loadedSteps.incrementAndGet();
        activity.runOnUiThread(() -> listener.onProgress(loaded, TOTAL_STEPS));
    }

    private void markComplete() {
        finished.set(true);
        listener.onComplete();
        shutdown();
    }
}
