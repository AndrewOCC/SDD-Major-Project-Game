package com.aocc.majorproject;

import com.aocc.framework.implementation.RotationHandler;
import com.aocc.framework.PersonalMethods;

/**
 * Shared sound, music, and tilt preference logic for canvas and Compose UI.
 */
public final class GameSettings {

    private GameSettings() {
    }

    public static void toggleSound() {
        playTap();
        if (!MainMenuScreen.sound) {
            MainMenuScreen.sound = true;
            MainMenuScreen.tapVol = 10;
        } else {
            MainMenuScreen.sound = false;
            MainMenuScreen.tapVol = 0;
        }
    }

    public static void toggleMusic() {
        playTap();
        if (!MainMenuScreen.music) {
            MainMenuScreen.music = true;
            Assets.setMusicVolume(0.25f);
        } else {
            MainMenuScreen.music = false;
            Assets.setMusicVolume(0);
        }
    }

    public static void applyFlatTilt(Player player) {
        playTap();
        player.setxBias(0);
        player.setyBias(0);
        player.setTiltMode(1);
    }

    public static void applyTiltedTilt(Player player) {
        playTap();
        player.setxBias(0);
        player.setyBias(-0.30f);
        player.setTiltMode(2);
    }

    public static void applyCustomTilt(Player player) {
        playTap();
        player.setxBias(-PersonalMethods.limitInside(RotationHandler.getRotationX(), -90, 90) / 90);
        player.setyBias(-PersonalMethods.limitInside(RotationHandler.getRotationY(), -90, 90) / 90);
        player.setTiltMode(3);
    }

    private static void playTap() {
        if (Assets.tap != null) {
            Assets.tap.play(MainMenuScreen.tapVol);
        }
    }
}
