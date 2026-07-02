package com.aocc.majorproject.ui

import android.graphics.Paint
import com.aocc.framework.Graphics

/** Text rendering with correct horizontal and vertical alignment within bounds. */
object UiText {

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
        val fm = paint.fontMetrics
        val baseline = centerY - (fm.ascent + fm.descent) / 2f
        g.drawString(text, anchorX, baseline.toInt(), color, paint)
    }
}
