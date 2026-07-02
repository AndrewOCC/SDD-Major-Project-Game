package com.aocc.majorproject;

import com.aocc.framework.implementation.RotationHandler;
import com.aocc.framework.PersonalMethods;

/**
 * Shared sound, music, and tilt preference logic. Persists via {@link GamePreferences}.
 */
public final class GameSettings {

    private GameSettings() {
    }

    public static void toggleSound() {
        playTap();
        GamePreferences.setSound(!GamePreferences.sound);
    }

    public static void toggleMusic() {
        playTap();
        GamePreferences.setMusic(!GamePreferences.music);
        if (GamePreferences.music) {
            Assets.setMusicVolume(0.25f);
        } else {
            Assets.setMusicVolume(0);
        }
    }

    public static void toggleSecondScreen(MajorProjectGame game) {
        playTap();
        GamePreferences.setSecondScreenEnabled(!GamePreferences.secondScreenEnabled);
        if (game != null) {
            game.getSecondaryDisplayManager().updateForScreen(game.getCurrentScreen());
            if (GamePreferences.secondScreenEnabled
                    && !game.getSecondaryDisplayManager().isSecondaryDisplayAvailable()) {
                game.showSecondDisplayUnavailable();
            }
        }
    }

    public static void applyFlatTilt(Player player) {
        playTap();
        player.setxBias(0);
        player.setyBias(0);
        player.setTiltMode(1);
        GamePreferences.setTiltMode(1);
    }

    public static void applyTiltedTilt(Player player) {
        playTap();
        player.setxBias(0);
        player.setyBias(-0.30f);
        player.setTiltMode(2);
        GamePreferences.setTiltMode(2);
    }

    public static void applyCustomTilt(Player player) {
        playTap();
        player.setxBias(-PersonalMethods.limitInside(RotationHandler.getRotationX(), -90, 90) / 90);
        player.setyBias(-PersonalMethods.limitInside(RotationHandler.getRotationY(), -90, 90) / 90);
        player.setTiltMode(3);
        GamePreferences.setTiltMode(3);
    }

    private static void playTap() {
        if (Assets.tap != null) {
            Assets.tap.play(GamePreferences.getTapVolume());
        }
    }
}
