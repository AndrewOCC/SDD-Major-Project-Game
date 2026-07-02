package com.aocc.majorproject.ui;

import com.aocc.framework.GameConstants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MainMenuLayoutTest {

    @Test
    public void playAndTutorialButtons_areCenteredInWorld() {
        UiBounds play = MainMenuLayout.playButton();
        UiBounds tutorial = MainMenuLayout.tutorialButton();

        int rowWidth = MainMenuLayout.BUTTON_WIDTH * 2 + MainMenuLayout.BUTTON_GAP;
        assertEquals(UiLayout.centerX(rowWidth), play.x);
        assertEquals(play.x + MainMenuLayout.BUTTON_WIDTH + MainMenuLayout.BUTTON_GAP, tutorial.x);
        assertEquals(MainMenuLayout.BUTTON_Y, play.y);
        assertEquals(MainMenuLayout.BUTTON_Y, tutorial.y);
    }

    @Test
    public void gpgButtons_alignToRightEdge() {
        UiBounds leaderboards = MainMenuLayout.leaderboardsButton();
        assertEquals(GameConstants.WORLD_WIDTH - MainMenuLayout.GPG_BUTTON_WIDTH
                - MainMenuLayout.GPG_MARGIN, leaderboards.x);
    }

    @Test
    public void playHighlight_isInsetFromTouchTarget() {
        UiBounds touch = MainMenuLayout.playButton();
        UiBounds highlight = MainMenuLayout.highlightForIndex(0, true);
        assertTrue(highlight.width < touch.width);
        assertTrue(highlight.height < touch.height);
    }
}
