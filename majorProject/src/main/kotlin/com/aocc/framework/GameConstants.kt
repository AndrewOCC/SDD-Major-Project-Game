package com.aocc.framework

/**
 * Shared virtual resolution and timing reference for framerate-independent simulation.
 * Game logic was originally tuned at 60 FPS against a 1280×720 world coordinate space.
 * Rendering scales this world to the device's native resolution via [Viewport].
 */
object GameConstants {

    const val WORLD_WIDTH = 1280
    const val WORLD_HEIGHT = 720
    const val REFERENCE_FPS = 60f
    const val MAX_DELTA_SECONDS = 0.05f
    const val SPEED_RAMP_INTERVAL_FRAMES = 500f

    /** Speed-ramp steps at which the game reaches its fastest pace. */
    const val SPEED_RAMP_MAX = 25

    /**
     * Top of the playable area. The readouts are projected onto the floor now, so there is no
     * HUD band; this is just the inside face of the window wall.
     */
    const val PLAY_AREA_TOP = 30

    /** Converts a frame-based constant to seconds using the 60 FPS reference. */
    @JvmStatic
    fun framesToSeconds(frames: Float): Float {
        return frames / REFERENCE_FPS
    }

    /** Converts elapsed seconds to equivalent frame steps at the 60 FPS reference. */
    @JvmStatic
    fun secondsToSteps(deltaSeconds: Float): Float {
        return deltaSeconds * REFERENCE_FPS
    }
}
