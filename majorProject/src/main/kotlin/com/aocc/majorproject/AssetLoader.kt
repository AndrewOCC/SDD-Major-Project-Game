package com.aocc.majorproject

import android.app.Activity
import android.graphics.Typeface
import com.aocc.framework.Audio
import com.aocc.framework.Graphics
import com.aocc.framework.Graphics.ImageFormat
import com.aocc.framework.Image
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Loads game assets on a background thread and reports progress to the loading screen.
 */
class AssetLoader(
    game: MajorProjectGame,
    private val listener: Listener,
) {

    interface Listener {
        fun onProgress(loaded: Int, total: Int)
        fun onComplete()
        fun onError(error: Exception)
    }

    private val activity: Activity = game
    private val game: MajorProjectGame = game
    private val graphics: Graphics = game.graphics
    private val audio: Audio = game.audio
    private val pixelScale: Int = AssetScale.tierFromViewport(game.viewport.getScale())
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val loadedSteps = AtomicInteger()
    private val started = AtomicBoolean(false)
    private val finished = AtomicBoolean(false)

    init {
        Assets.loadedPixelScale = pixelScale
    }

    fun start() {
        if (!started.compareAndSet(false, true)) {
            return
        }

        executor.execute {
            try {
                loadSplash()
                loadImages()
                loadSounds()
                loadFonts()

                activity.runOnUiThread {
                    try {
                        Assets.loadMusic(game)
                        stepComplete()
                        markComplete()
                    } catch (e: RuntimeException) {
                        listener.onError(e)
                        shutdown()
                    }
                }
            } catch (e: RuntimeException) {
                activity.runOnUiThread {
                    listener.onError(e)
                    shutdown()
                }
            }
        }
    }

    fun isFinished(): Boolean = finished.get()

    fun getProgress(): Float = loadedSteps.get() / TOTAL_STEPS.toFloat()

    fun getLoadedSteps(): Int = loadedSteps.get()

    fun getTotalSteps(): Int = TOTAL_STEPS

    fun getPixelScale(): Int = pixelScale

    fun shutdown() {
        executor.shutdownNow()
    }

    private fun loadSplash() {
        Assets.splash = loadImage("splash.png", ImageFormat.RGB565, false)
        stepComplete()
    }

    private fun loadImages() {
        Assets.noise = loadImage("noise.png", ImageFormat.ARGB4444, false)
        stepComplete()
        Assets.menu_bg = loadImage("menu-bg.png", ImageFormat.RGB565, false)
        stepComplete()
        Assets.game_bg = loadImage("game-bg.png", ImageFormat.RGB565, false)
        stepComplete()
        Assets.sign_in_base = loadImage("Red-signin_Medium_base.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.sign_in_press = loadImage("Red-signin_Medium_press.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.gpg_icon_leaderboards = loadImage("gpg-icon-leaderboards.png", ImageFormat.RGB565, true)
        stepComplete()
        Assets.gpg_icon_achievements = loadImage("gpg-icon-achievements.png", ImageFormat.RGB565, true)
        stepComplete()
        Assets.tilt_control_flat = loadImage("tilt-button-flat.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.tilt_control_tilted = loadImage("tilt-button-tilted.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.tilt_control_custom = loadImage("tilt-button-custom.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.tilt_control_flat_2 = loadImage("tilt-button-flat-2.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.tilt_control_tilted_2 = loadImage("tilt-button-tilted-2.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.tilt_control_custom_2 = loadImage("tilt-button-custom-2.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.sound = loadImage("sound.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.sound_muted = loadImage("sound-muted.png", ImageFormat.ARGB4444, true)
        Assets.music = loadImage("music.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.music_muted = loadImage("music-muted.png", ImageFormat.ARGB4444, true)
        stepComplete()
        Assets.tutorial = loadImage("tutorial.png", ImageFormat.RGB565, false)
        stepComplete()
    }

    /** Full-screen backgrounds stay at 1×; UI sprites may use 2×. */
    private fun loadImage(path: String, format: ImageFormat, allow2x: Boolean): Image {
        val scale = if (allow2x) pixelScale else 1
        return graphics.newImage(path, format, scale)
    }

    private fun loadSounds() {
        Assets.tap = audio.createSound("tap.wav")
        stepComplete()
        Assets.zap = audio.createSound("zap.wav")
        stepComplete()
        Assets.burn = audio.createSound("burn.wav")
        stepComplete()
        Assets.powerup = audio.createSound("powerup.wav")
        stepComplete()
    }

    private fun loadFonts() {
        Assets.plain = Typeface.createFromAsset(game.assets, "fonts/power_clear.ttf")
        Assets.bold = Typeface.create(Assets.plain, Typeface.BOLD)
        stepComplete()
    }

    private fun stepComplete() {
        val loaded = loadedSteps.incrementAndGet()
        activity.runOnUiThread { listener.onProgress(loaded, TOTAL_STEPS) }
    }

    private fun markComplete() {
        finished.set(true)
        listener.onComplete()
        shutdown()
    }

    companion object {
        const val TOTAL_STEP_COUNT = 23
        private const val TOTAL_STEPS = TOTAL_STEP_COUNT
    }
}
