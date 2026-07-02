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

    public void shutdown() {
        executor.shutdownNow();
    }

    private void loadSplash() {
        Assets.splash = graphics.newImage("splash.png", ImageFormat.RGB565);
        stepComplete();
    }

    private void loadImages() {
        Assets.noise = graphics.newImage("noise.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.menu_bg = graphics.newImage("menu-bg.png", ImageFormat.RGB565);
        stepComplete();
        Assets.game_bg = graphics.newImage("game-bg.png", ImageFormat.RGB565);
        stepComplete();
        Assets.sign_in_base = graphics.newImage("Red-signin_Medium_base.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.sign_in_press = graphics.newImage("Red-signin_Medium_press.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.gpg_icon_leaderboards = graphics.newImage("gpg-icon-leaderboards.png", ImageFormat.RGB565);
        stepComplete();
        Assets.gpg_icon_achievements = graphics.newImage("gpg-icon-achievements.png", ImageFormat.RGB565);
        stepComplete();
        Assets.tilt_control_flat = graphics.newImage("tilt-button-flat.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.tilt_control_tilted = graphics.newImage("tilt-button-tilted.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.tilt_control_custom = graphics.newImage("tilt-button-custom.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.tilt_control_flat_2 = graphics.newImage("tilt-button-flat-2.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.tilt_control_tilted_2 = graphics.newImage("tilt-button-tilted-2.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.tilt_control_custom_2 = graphics.newImage("tilt-button-custom-2.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.sound = graphics.newImage("sound.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.sound_muted = graphics.newImage("sound-muted.png", ImageFormat.ARGB4444);
        Assets.music = graphics.newImage("music.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.music_muted = graphics.newImage("music-muted.png", ImageFormat.ARGB4444);
        stepComplete();
        Assets.tutorial = graphics.newImage("tutorial.png", ImageFormat.RGB565);
        stepComplete();
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
