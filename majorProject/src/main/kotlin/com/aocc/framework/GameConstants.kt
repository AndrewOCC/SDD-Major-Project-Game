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

    /**
     * Height of the reserved HUD band at the top of the screen (score / combo strip).
     * Gameplay — the player, enemies and power-ups — is confined below this line so
     * nothing overlaps the readouts. Slightly taller than the 50px score bar for margin.
     */
    const val PLAY_AREA_TOP = 56

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
