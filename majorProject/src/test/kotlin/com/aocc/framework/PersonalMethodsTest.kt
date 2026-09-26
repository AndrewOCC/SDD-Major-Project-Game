package com.aocc.framework

import android.graphics.Point
import android.graphics.RectF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PersonalMethodsTest {

    @Test
    fun limitInside_clampsBelowMinimum() {
        assertEquals(0f, PersonalMethods.limitInside(-5f, 0, 10), 0.001f)
    }

    @Test
    fun limitInside_clampsAboveMaximum() {
        assertEquals(10f, PersonalMethods.limitInside(25f, 0, 10), 0.001f)
    }

    @Test
    fun limitInside_keepsValueInsideRange() {
        assertEquals(5f, PersonalMethods.limitInside(5f, 0, 10), 0.001f)
    }

    @Test
    fun touchInBounds_returnsTrueForPointInsideRectangle() {
        val event = Input.TouchEvent()
        event.x = 105
        event.y = 105

        assertTrue(PersonalMethods.touchInBounds(event, 100, 100, 20, 20))
    }

    @Test
    fun touchInBounds_returnsFalseForPointOutsideRectangle() {
        val event = Input.TouchEvent()
        event.x = 130
        event.y = 105

        assertFalse(PersonalMethods.touchInBounds(event, 100, 100, 20, 20))
    }

    @Test
    fun rectFInBounds_detectsOverlappingRectangles() {
        val player = RectF(100f, 100f, 150f, 150f)
        val enemy = RectF(140f, 140f, 180f, 180f)

        assertTrue(PersonalMethods.rectFInBounds(player, 0, enemy))
    }

    @Test
    fun rectFInBounds_respectsBufferDistance() {
        val player = RectF(100f, 100f, 150f, 150f)
        val enemy = RectF(170f, 170f, 200f, 200f)

        assertFalse(PersonalMethods.rectFInBounds(player, 0, enemy))
        assertTrue(PersonalMethods.rectFInBounds(player, 25, enemy))
    }

    @Test
    fun limitOutside_pushesPointBeyondVerticalBoundary() {
        val point = Point(100, 105)

        val adjusted = PersonalMethods.limitOutside(point, 100, 100, 20)

        assertEquals(100, adjusted.x)
        assertEquals(120, adjusted.y)
    }
}
