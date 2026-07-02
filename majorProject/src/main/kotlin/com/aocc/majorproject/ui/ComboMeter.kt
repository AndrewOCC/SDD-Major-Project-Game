package com.aocc.majorproject.ui

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics

/** Combo multiplier strip shown to the left of the score bar. */
class ComboMeter {

    private val bounds = UiBounds(BAR_X, 0, BAR_WIDTH, BAR_HEIGHT)
    private val backgroundColor = Color.argb(100, 255, 255, 255)
    private val textColor = Color.WHITE
    private val textSize = 40f

    fun getBounds(): UiBounds = bounds

    fun paint(g: Graphics, paint: Paint, combo: Int) {
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, backgroundColor)
        val previousSize = paint.textSize
        paint.textSize = textSize
        UiText.drawInBounds(g, paint, "x$combo", bounds, UiText.HAlign.CENTER, textColor)
        paint.textSize = previousSize
    }

    companion object {
        private const val BAR_HEIGHT = ScoreBar.BAR_HEIGHT
        private const val BAR_WIDTH = 120
        private const val GAP = 8
        private val BAR_X: Int = ScoreBar.BAR_X - BAR_WIDTH - GAP
    }
}
