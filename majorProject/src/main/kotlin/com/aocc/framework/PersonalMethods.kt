package com.aocc.framework

import android.graphics.Point
import android.graphics.RectF
import com.aocc.framework.Input.TouchEvent

// This is the one class created entirely by myself within the framework. It handles
// various different functions which I call repeatedly throughout the project, and
// is designed to be reusable.

object PersonalMethods {

    @JvmStatic
    fun limitInside(value: Float, lowerLimit: Int, upperLimit: Int): Float {
        // Method to limit a value between two other values. Math.max returns the
        // largest of two values, and Math.min the smallest
        return maxOf(lowerLimit.toFloat(), minOf(value, upperLimit.toFloat()))
    }

    @JvmStatic
    fun limitOutside(value: Point, centerX: Int, centerY: Int, distance: Int): Point {
        // Method to determine if a value (point2) is a certain distance from
        // another value (point1)

        if (value.x < centerX + distance && value.x > centerX - distance) {
            if (value.y <= centerY) {
                value.y = minOf(value.y, centerY - distance)
            } else {
                value.y = maxOf(value.y, centerY + distance)
            }
        }
        if (value.y < centerY + distance && value.y > centerY - distance) {
            if (value.x <= centerX) {
                value.x = minOf(value.x, centerX - distance)
            } else {
                value.x = maxOf(value.x, centerX + distance)
            }
        }

        return value
    }

    @JvmStatic
    fun touchInBounds(event: TouchEvent, x: Int, y: Int, width: Int, height: Int): Boolean {
        //handles rectangular collision
        return event.x > x && event.x < x + width - 1
                && event.y > y && event.y < y + height - 1
    }

    //rectangle collision code
    @JvmStatic
    fun rectFInBounds(rect1: RectF, buffer: Int, rect2: RectF): Boolean {
        return rect1.left - buffer < rect2.right && rect1.right + buffer > rect2.left &&
                rect1.top - buffer < rect2.bottom && rect1.bottom + buffer > rect2.top
    }
}
