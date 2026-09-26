package com.aocc.majorproject.ui;

import com.aocc.framework.Input;
import com.aocc.framework.PersonalMethods;

/** Main menu hit regions anchored to the world rectangle (works with viewport letterboxing). */
public final class MainMenuLayout {

    public static final int BUTTON_WIDTH = 440;
    public static final int BUTTON_HEIGHT = 200;
    public static final int BUTTON_Y = 435;
    public static final int BUTTON_GAP = 80;

    public static final int GPG_BUTTON_WIDTH = 100;
    public static final int GPG_BUTTON_HEIGHT = 80;
    public static final int GPG_MARGIN = 5;

    public static final int SIGN_IN_WIDTH = 180;
    public static final int SIGN_IN_HEIGHT = 60;
    public static final int SIGN_IN_MARGIN = 7;

    private MainMenuLayout() {
    }

    public static UiBounds playButton() {
        int rowWidth = BUTTON_WIDTH * 2 + BUTTON_GAP;
        int rowX = UiLayout.centerX(rowWidth);
        return new UiBounds(rowX, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public static UiBounds tutorialButton() {
        UiBounds play = playButton();
        return new UiBounds(play.x + BUTTON_WIDTH + BUTTON_GAP, BUTTON_Y,
                BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    public static UiBounds leaderboardsButton() {
        return new UiBounds(UiLayout.alignRight(GPG_BUTTON_WIDTH, GPG_MARGIN), GPG_MARGIN,
                GPG_BUTTON_WIDTH, GPG_BUTTON_HEIGHT);
    }

    public static UiBounds achievementsButton() {
        UiBounds leaderboards = leaderboardsButton();
        return new UiBounds(leaderboards.x, leaderboards.y + GPG_BUTTON_HEIGHT,
                GPG_BUTTON_WIDTH, GPG_BUTTON_HEIGHT);
    }

    public static UiBounds signInButton() {
        return new UiBounds(SIGN_IN_MARGIN, SIGN_IN_MARGIN, SIGN_IN_WIDTH, SIGN_IN_HEIGHT);
    }

    public static boolean isPlay(Input.TouchEvent event) {
        return playButton().contains(event);
    }

    public static boolean isTutorial(Input.TouchEvent event) {
        return tutorialButton().contains(event);
    }

    public static boolean isLeaderboards(Input.TouchEvent event) {
        return leaderboardsButton().contains(event);
    }

    public static boolean isAchievements(Input.TouchEvent event) {
        return achievementsButton().contains(event);
    }

    public static boolean isSignIn(Input.TouchEvent event) {
        return signInButton().contains(event);
    }

    public static int leaderboardsDrawX() {
        return leaderboardsButton().x;
    }

    public static int leaderboardsDrawY() {
        return leaderboardsButton().y;
    }

    public static int achievementsDrawX() {
        return achievementsButton().x;
    }

    public static int achievementsDrawY() {
        return achievementsButton().y;
    }

    public static int signInDrawX() {
        return signInButton().x;
    }

    public static int signInDrawY() {
        return signInButton().y;
    }

    public static boolean touchInBounds(Input.TouchEvent event, UiBounds bounds) {
        return PersonalMethods.touchInBounds(event, bounds.x, bounds.y,
                bounds.width, bounds.height);
    }
}
