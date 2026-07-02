package com.aocc.majorproject.ui

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics

/** Top-centre score strip with semi-transparent background and centred label. */
class ScoreBar {

    private val bounds = UiBounds(BAR_X, 0, BAR_WIDTH, BAR_HEIGHT)
    private val backgroundColor = Color.argb(100, 255, 255, 255)
    private val textColor = Color.WHITE
    private val textSize = 40f

    fun paint(g: Graphics, paint: Paint, score: Int) {
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, backgroundColor)
        val previousSize = paint.textSize
        paint.textSize = textSize
        UiText.drawInBounds(g, paint, "SCORE: $score", bounds, UiText.HAlign.CENTER, textColor)
        paint.textSize = previousSize
    }

    companion object {
        const val BAR_WIDTH = 320
        const val BAR_HEIGHT = 50
        val BAR_X: Int = (com.aocc.framework.GameConstants.WORLD_WIDTH - BAR_WIDTH) / 2
    }
}
