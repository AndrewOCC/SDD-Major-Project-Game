package com.aocc.majorproject.ui;

import com.aocc.framework.Input;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UiButtonTest {

    private UiButton retryButton;

    @Before
    public void setUp() {
        retryButton = new UiButton(540, 500, 200, 100, "Retry");
    }

    @Test
    public void touchInBounds_returnsTrueInsideButton() {
        Input.TouchEvent event = new Input.TouchEvent();
        event.x = 640;
        event.y = 550;
        assertTrue(retryButton.touchInBounds(event));
    }

    @Test
    public void touchInBounds_returnsFalseOutsideButton() {
        Input.TouchEvent event = new Input.TouchEvent();
        event.x = 100;
        event.y = 65;
        assertFalse(retryButton.touchInBounds(event));
    }
}
