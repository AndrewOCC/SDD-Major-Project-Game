package com.aocc.majorproject;

import com.aocc.framework.Input;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ButtonTest {

    private Button menuButton;

    @Before
    public void setUp() {
        menuButton = new Button(0, 0, 4, 0, "Menu");
    }

    @Test
    public void touchInBounds_returnsTrueInsideMenuButton() {
        Input.TouchEvent event = new Input.TouchEvent();
        event.x = 100;
        event.y = 50;

        assertTrue(menuButton.touchInBounds(event));
    }

    @Test
    public void touchInBounds_returnsFalseOutsideMenuButton() {
        Input.TouchEvent event = new Input.TouchEvent();
        event.x = 250;
        event.y = 50;

        assertFalse(menuButton.touchInBounds(event));
    }

    @Test
    public void touchInBounds_respectsMainMenuButtonDimensions() {
        Button playButton = new Button(420, 260, 1, 0, "Play");

        Input.TouchEvent inside = new Input.TouchEvent();
        inside.x = 500;
        inside.y = 300;
        Input.TouchEvent outside = new Input.TouchEvent();
        outside.x = 900;
        outside.y = 300;

        assertTrue(playButton.touchInBounds(inside));
        assertFalse(playButton.touchInBounds(outside));
    }
}
