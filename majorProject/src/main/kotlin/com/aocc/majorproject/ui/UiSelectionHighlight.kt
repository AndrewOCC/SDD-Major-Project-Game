package com.aocc.majorproject.ui

import android.graphics.Color
import com.aocc.framework.Graphics

/** Gamepad / keyboard focus ring — outline only, does not cover button art. */
object UiSelectionHighlight {

    @JvmField
    val COLOR: Int = Color.rgb(255, 220, 80)

    @JvmField
    val STROKE_WIDTH: Float = 3f

    @JvmStatic
    fun paintRect(g: Graphics, bounds: UiBounds) {
        g.drawRectOutline(bounds.x, bounds.y, bounds.width, bounds.height, COLOR, STROKE_WIDTH)
    }

    @JvmStatic
    fun paintCircle(g: Graphics, centerX: Int, centerY: Int, radius: Int) {
        g.drawCircleOutline(
            centerX.toFloat(),
            centerY.toFloat(),
            radius.toFloat(),
            COLOR,
            STROKE_WIDTH
        )
    }
}
