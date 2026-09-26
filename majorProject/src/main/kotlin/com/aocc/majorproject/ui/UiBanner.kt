package com.aocc.majorproject.ui

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics

/** Large centred prompt or heading text (e.g. "Game Over!", "Press anywhere to start"). */
class UiBanner(
    private val textSize: Float,
    private val color: Int = Color.WHITE,
) {

    constructor(textSize: Float) : this(textSize, Color.WHITE)

    fun paint(g: Graphics, paint: Paint, text: String, centerX: Int, centerY: Int) {
        val previousSize = paint.textSize
        paint.textSize = textSize
        UiText.drawCentered(g, paint, text, centerX, centerY, color)
        paint.textSize = previousSize
    }
}
