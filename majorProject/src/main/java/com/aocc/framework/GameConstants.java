package com.aocc.framework;

/**
 * Shared virtual resolution and timing reference for framerate-independent simulation.
 * Game logic was originally tuned at 60 FPS against a 1280x720 world.
 */
public final class GameConstants {

    public static final int WORLD_WIDTH = 1280;
    public static final int WORLD_HEIGHT = 720;
    public static final float REFERENCE_FPS = 60f;
    public static final float MAX_DELTA_SECONDS = 0.05f;
    public static final float SPEED_RAMP_INTERVAL_FRAMES = 500f;

    private GameConstants() {
    }

    /** Converts a frame-based constant to seconds using the 60 FPS reference. */
    public static float framesToSeconds(float frames) {
        return frames / REFERENCE_FPS;
    }

    /** Converts elapsed seconds to equivalent frame steps at the 60 FPS reference. */
    public static float secondsToSteps(float deltaSeconds) {
        return deltaSeconds * REFERENCE_FPS;
    }
}
