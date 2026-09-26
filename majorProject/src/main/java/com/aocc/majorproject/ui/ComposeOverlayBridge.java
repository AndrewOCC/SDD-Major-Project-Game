package com.aocc.majorproject.ui;

import com.aocc.majorproject.Player;

/**
 * Java-facing bridge to the Jetpack Compose menu overlay hosted by {@code AndroidGame}.
 */
public interface ComposeOverlayBridge {

    interface MainMenuListener {
        void onPlay();

        void onTutorial();

        void onSignIn();

        void onLeaderboards();

        void onAchievements();

        boolean isLoggedIn();
    }

    interface SettingsListener {
        void onResumeGame();

        void onMenu();

        void onToggleSound();

        void onToggleMusic();

        void onFlatTilt();

        void onTiltedTilt();

        void onCustomTilt();

        boolean isSoundOn();

        boolean isMusicOn();

        int getTiltMode();
    }

    void showMainMenu(MainMenuListener listener);

    void showSettings(String prompt, boolean showMenuButton, SettingsListener listener);

    void hide();

    void refreshSettings();
}
