package com.aocc.majorproject.ui;

import com.aocc.framework.Input;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UiButtonTest {

    private UiButton retryButton;

    @Before
    public void setUp() {
        retryButton = new UiButton(540, 500, UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT, "Retry");
    }

    @Test
    public void touchInBounds_returnsTrueInsideButton() {
        Input.TouchEvent event = new Input.TouchEvent();
        event.x = 640;
        event.y = 550;
        assertTrue(retryButton.touchInBounds(event));
    }

    @Test
    public void menuAt_usesStandardMenuDimensions() {
        UiButton menu = UiButton.menuAt(0, 0);
        assertEquals(UiButton.MENU_WIDTH, menu.getBounds().width);
        assertEquals(UiButton.MENU_HEIGHT, menu.getBounds().height);
    }

    @Test
    public void touchInBounds_returnsFalseOutsideButton() {
        Input.TouchEvent event = new Input.TouchEvent();
        event.x = 100;
        event.y = 65;
        assertFalse(retryButton.touchInBounds(event));
    }
}
