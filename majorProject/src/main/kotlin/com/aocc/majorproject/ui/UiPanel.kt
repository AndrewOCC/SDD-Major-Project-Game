package com.aocc.majorproject.ui

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics

/** Filled panel with an optional centred title above the panel body. */
class UiPanel(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val backgroundColor: Int,
) {

    private val bounds = UiBounds(x, y, width, height)
    private val titleTextSize = 30f
    private val titleColor = Color.WHITE
    private val titleGap = 30

    fun getBounds(): UiBounds = bounds

    fun paintBackground(g: Graphics) {
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, backgroundColor)
    }

    fun paintTitle(g: Graphics, paint: Paint, title: String) {
        val previousSize = paint.textSize
        paint.textSize = titleTextSize
        val titleCenterY = bounds.y - titleGap
        UiText.drawCentered(g, paint, title, bounds.centerX(), titleCenterY, titleColor)
        paint.textSize = previousSize
    }
}
