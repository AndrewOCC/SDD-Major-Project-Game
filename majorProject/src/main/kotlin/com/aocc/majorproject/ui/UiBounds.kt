package com.aocc.majorproject.ui

import com.aocc.framework.Input

/** Axis-aligned rectangle in world coordinates (1280×720). */
data class UiBounds(
    @JvmField val x: Int,
    @JvmField val y: Int,
    @JvmField val width: Int,
    @JvmField val height: Int,
) {

    fun centerX(): Int = x + width / 2

    fun centerY(): Int = y + height / 2

    fun contains(touchX: Float, touchY: Float): Boolean {
        return touchX > x && touchX < x + width - 1
            && touchY > y && touchY < y + height - 1
    }

    fun contains(event: Input.TouchEvent): Boolean {
        return contains(event.x.toFloat(), event.y.toFloat())
    }

    /** Returns a smaller rectangle inset on all sides (for visual focus rings). */
    fun inset(horizontal: Int, vertical: Int): UiBounds {
        return UiBounds(
            x + horizontal,
            y + vertical,
            width - horizontal * 2,
            height - vertical * 2
        )
    }
}
