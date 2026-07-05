package com.aocc.majorproject.ui

import android.graphics.Paint
import android.graphics.Rect
import com.aocc.framework.Graphics

/** Text rendering with correct horizontal and vertical alignment within bounds. */
object UiText {

    private val measureRect = Rect()

    enum class HAlign(private val paintAlign: Paint.Align) {
        LEFT(Paint.Align.LEFT),
        CENTER(Paint.Align.CENTER),
        RIGHT(Paint.Align.RIGHT),
    }

    @JvmStatic
    fun drawInBounds(
        g: Graphics,
        paint: Paint,
        text: String,
        bounds: UiBounds,
        hAlign: HAlign,
        color: Int,
    ) {
        paint.textAlign = when (hAlign) {
            HAlign.LEFT -> Paint.Align.LEFT
            HAlign.RIGHT -> Paint.Align.RIGHT
            HAlign.CENTER -> Paint.Align.CENTER
        }
        val anchorX = when (hAlign) {
            HAlign.LEFT -> bounds.x
            HAlign.RIGHT -> bounds.x + bounds.width
            HAlign.CENTER -> bounds.centerX()
        }
        drawAtBaseline(g, paint, text, anchorX, bounds.centerY(), color)
    }

    @JvmStatic
    fun drawCentered(
        g: Graphics,
        paint: Paint,
        text: String,
        centerX: Int,
        centerY: Int,
        color: Int,
    ) {
        paint.textAlign = Paint.Align.CENTER
        drawAtBaseline(g, paint, text, centerX, centerY, color)
    }

    @JvmStatic
    fun drawLeftOfCenter(
        g: Graphics,
        paint: Paint,
        text: String,
        leftX: Int,
        centerY: Int,
        color: Int,
    ) {
        paint.textAlign = Paint.Align.LEFT
        drawAtBaseline(g, paint, text, leftX, centerY, color)
    }

    private fun drawAtBaseline(
        g: Graphics,
        paint: Paint,
        text: String,
        anchorX: Int,
        centerY: Int,
        color: Int,
    ) {
        // Centre on the actual glyph ink box rather than font metrics; the custom
        // game font ("power_clear") has asymmetric ascent/descent padding that
        // otherwise leaves labels visibly high inside their bounds.
        if (text.isEmpty()) {
            return
        }
        paint.getTextBounds(text, 0, text.length, measureRect)
        val baseline = centerY - (measureRect.top + measureRect.bottom) / 2f
        g.drawString(text, anchorX, Math.round(baseline), color, paint)
    }
}
