package com.aocc.framework;

import android.graphics.Point;
import android.graphics.RectF;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class PersonalMethodsTest {

    @Test
    public void limitInside_clampsBelowMinimum() {
        assertEquals(0f, PersonalMethods.limitInside(-5f, 0, 10), 0.001f);
    }

    @Test
    public void limitInside_clampsAboveMaximum() {
        assertEquals(10f, PersonalMethods.limitInside(25f, 0, 10), 0.001f);
    }

    @Test
    public void limitInside_keepsValueInsideRange() {
        assertEquals(5f, PersonalMethods.limitInside(5f, 0, 10), 0.001f);
    }

    @Test
    public void touchInBounds_returnsTrueForPointInsideRectangle() {
        Input.TouchEvent event = new Input.TouchEvent();
        event.x = 105;
        event.y = 105;

        assertTrue(PersonalMethods.touchInBounds(event, 100, 100, 20, 20));
    }

    @Test
    public void touchInBounds_returnsFalseForPointOutsideRectangle() {
        Input.TouchEvent event = new Input.TouchEvent();
        event.x = 130;
        event.y = 105;

        assertFalse(PersonalMethods.touchInBounds(event, 100, 100, 20, 20));
    }

    @Test
    public void rectFInBounds_detectsOverlappingRectangles() {
        RectF player = new RectF(100, 100, 150, 150);
        RectF enemy = new RectF(140, 140, 180, 180);

        assertTrue(PersonalMethods.rectFInBounds(player, 0, enemy));
    }

    @Test
    public void rectFInBounds_respectsBufferDistance() {
        RectF player = new RectF(100, 100, 150, 150);
        RectF enemy = new RectF(170, 170, 200, 200);

        assertFalse(PersonalMethods.rectFInBounds(player, 0, enemy));
        assertTrue(PersonalMethods.rectFInBounds(player, 25, enemy));
    }

    @Test
    public void limitOutside_pushesPointBeyondVerticalBoundary() {
        Point point = new Point(100, 105);

        Point adjusted = PersonalMethods.limitOutside(point, 100, 100, 20);

        assertEquals(100, adjusted.x);
        assertEquals(120, adjusted.y);
    }
}
