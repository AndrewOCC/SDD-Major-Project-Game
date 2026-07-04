package com.aocc.majorproject

import android.content.Context
import android.media.AudioManager
import com.aocc.framework.PersonalMethods
import com.aocc.framework.implementation.RotationHandler

/**
 * Persisted player preferences. Loaded once at startup; saved on each change.
 */
object GamePreferences {

    private const val PREFS_NAME = "major_project_prefs"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_MUSIC = "music_enabled"
    private const val KEY_MUSIC_INITIALIZED = "music_initialized"
    private const val KEY_TILT_MODE = "tilt_mode"
    private const val KEY_SECOND_SCREEN = "second_screen_enabled"

    private var prefs: android.content.SharedPreferences? = null

    @JvmField
    var sound: Boolean = true

    @JvmField
    var music: Boolean = true

    @JvmField
    var tiltMode: Int = 2

    @JvmField
    var secondScreenEnabled: Boolean = false

    @JvmStatic
    fun load(context: Context) {
        val loaded = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = loaded
        sound = loaded.getBoolean(KEY_SOUND, true)
        tiltMode = loaded.getInt(KEY_TILT_MODE, 2)
        secondScreenEnabled = loaded.getBoolean(KEY_SECOND_SCREEN, false)

        if (!loaded.getBoolean(KEY_MUSIC_INITIALIZED, false)) {
            val audioManager =
                context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            music = audioManager == null || !audioManager.isMusicActive
            loaded.edit()
                .putBoolean(KEY_MUSIC, music)
                .putBoolean(KEY_MUSIC_INITIALIZED, true)
                .apply()
        } else {
            music = loaded.getBoolean(KEY_MUSIC, true)
        }
    }

    @JvmStatic
    fun getTapVolume(): Int {
        return if (sound) 10 else 0
    }

    @JvmStatic
    fun setSound(enabled: Boolean) {
        sound = enabled
        save()
    }

    @JvmStatic
    fun setMusic(enabled: Boolean) {
        music = enabled
        save()
    }

    @JvmStatic
    fun setTiltMode(mode: Int) {
        tiltMode = mode
        save()
    }

    @JvmStatic
    fun setSecondScreenEnabled(enabled: Boolean) {
        secondScreenEnabled = enabled
        save()
    }

    @JvmStatic
    fun applyTiltTo(player: Player) {
        when (tiltMode) {
            1 -> {
                player.xBias = 0f
                player.yBias = 0f
                player.tiltMode = 1
            }
            3 -> {
                player.xBias =
                    -PersonalMethods.limitInside(RotationHandler.getRotationX(), -90, 90) / 90f
                player.yBias =
                    -PersonalMethods.limitInside(RotationHandler.getRotationY(), -90, 90) / 90f
                player.tiltMode = 3
            }
            else -> {
                player.xBias = 0f
                player.yBias = -0.30f
                player.tiltMode = 2
            }
        }
    }

    private fun save() {
        prefs?.edit()
            ?.putBoolean(KEY_SOUND, sound)
            ?.putBoolean(KEY_MUSIC, music)
            ?.putInt(KEY_TILT_MODE, tiltMode)
            ?.putBoolean(KEY_SECOND_SCREEN, secondScreenEnabled)
            ?.apply()
    }
}
