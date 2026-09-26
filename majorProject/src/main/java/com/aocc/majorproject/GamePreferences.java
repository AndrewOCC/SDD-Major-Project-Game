package com.aocc.majorproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

import com.aocc.framework.implementation.RotationHandler;
import com.aocc.framework.PersonalMethods;

/**
 * Persisted player preferences. Loaded once at startup; saved on each change.
 */
public final class GamePreferences {

    private static final String PREFS_NAME = "major_project_prefs";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_MUSIC = "music_enabled";
    private static final String KEY_MUSIC_INITIALIZED = "music_initialized";
    private static final String KEY_TILT_MODE = "tilt_mode";
    private static final String KEY_SECOND_SCREEN = "second_screen_enabled";

    private static SharedPreferences prefs;

    public static boolean sound = true;
    public static boolean music = true;
    public static int tiltMode = 2;
    public static boolean secondScreenEnabled = false;

    private GamePreferences() {
    }

    public static void load(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sound = prefs.getBoolean(KEY_SOUND, true);
        tiltMode = prefs.getInt(KEY_TILT_MODE, 2);
        secondScreenEnabled = prefs.getBoolean(KEY_SECOND_SCREEN, false);

        if (!prefs.getBoolean(KEY_MUSIC_INITIALIZED, false)) {
            AudioManager audioManager =
                    (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            music = audioManager == null || !audioManager.isMusicActive();
            prefs.edit()
                    .putBoolean(KEY_MUSIC, music)
                    .putBoolean(KEY_MUSIC_INITIALIZED, true)
                    .apply();
        } else {
            music = prefs.getBoolean(KEY_MUSIC, true);
        }
    }

    public static int getTapVolume() {
        return sound ? 10 : 0;
    }

    public static void setSound(boolean enabled) {
        sound = enabled;
        save();
    }

    public static void setMusic(boolean enabled) {
        music = enabled;
        save();
    }

    public static void setTiltMode(int mode) {
        tiltMode = mode;
        save();
    }

    public static void setSecondScreenEnabled(boolean enabled) {
        secondScreenEnabled = enabled;
        save();
    }

    public static void applyTiltTo(Player player) {
        switch (tiltMode) {
            case 1:
                player.setxBias(0);
                player.setyBias(0);
                player.setTiltMode(1);
                break;
            case 3:
                player.setxBias(-PersonalMethods.limitInside(RotationHandler.getRotationX(), -90, 90) / 90);
                player.setyBias(-PersonalMethods.limitInside(RotationHandler.getRotationY(), -90, 90) / 90);
                player.setTiltMode(3);
                break;
            case 2:
            default:
                player.setxBias(0);
                player.setyBias(-0.30f);
                player.setTiltMode(2);
                break;
        }
    }

    private static void save() {
        if (prefs == null) {
            return;
        }
        prefs.edit()
                .putBoolean(KEY_SOUND, sound)
                .putBoolean(KEY_MUSIC, music)
                .putInt(KEY_TILT_MODE, tiltMode)
                .putBoolean(KEY_SECOND_SCREEN, secondScreenEnabled)
                .apply();
    }
}
