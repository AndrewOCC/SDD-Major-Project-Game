package com.aocc.majorproject

import com.aocc.framework.PersonalMethods
import com.aocc.framework.implementation.RotationHandler

/**
 * Shared sound, music, and tilt preference logic. Persists via [GamePreferences].
 */
object GameSettings {

    @JvmStatic
    fun toggleSound() {
        playTap()
        GamePreferences.setSound(!GamePreferences.sound)
    }

    @JvmStatic
    fun toggleMusic() {
        playTap()
        GamePreferences.setMusic(!GamePreferences.music)
        if (GamePreferences.music) {
            Assets.setMusicVolume(0.25f)
        } else {
            Assets.setMusicVolume(0f)
        }
    }

    @JvmStatic
    fun toggleSecondScreen(game: MajorProjectGame?) {
        playTap()
        GamePreferences.setSecondScreenEnabled(!GamePreferences.secondScreenEnabled)
        if (game != null) {
            game.secondaryDisplayManager.updateForScreen(game.currentScreen)
            if (GamePreferences.secondScreenEnabled
                && !game.secondaryDisplayManager.isSecondaryDisplayAvailable()
            ) {
                game.showSecondDisplayUnavailable()
            }
        }
    }

    @JvmStatic
    fun applyFlatTilt(player: Player) {
        playTap()
        player.xBias = 0f
        player.yBias = 0f
        player.tiltMode = 1
        GamePreferences.setTiltMode(1)
    }

    @JvmStatic
    fun applyTiltedTilt(player: Player) {
        playTap()
        player.xBias = 0f
        player.yBias = -0.30f
        player.tiltMode = 2
        GamePreferences.setTiltMode(2)
    }

    @JvmStatic
    fun applyCustomTilt(player: Player) {
        playTap()
        player.xBias =
            -PersonalMethods.limitInside(RotationHandler.getRotationX(), -90, 90) / 90f
        player.yBias =
            -PersonalMethods.limitInside(RotationHandler.getRotationY(), -90, 90) / 90f
        player.tiltMode = 3
        GamePreferences.setTiltMode(3)
    }

    private fun playTap() {
        Assets.tap?.play(GamePreferences.getTapVolume().toFloat())
    }
}
