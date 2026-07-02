package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics

object VersionOverlay {

    private const val VERSION_X = 1265
    private const val VERSION_Y = 710
    private const val VERSION_TEXT_SIZE = 18f

    private val VERSION_PAINT = Paint(Paint.ANTI_ALIAS_FLAG)

    @JvmStatic
    fun paint(g: Graphics) {
        if (Assets.plain != null) {
            VERSION_PAINT.typeface = Assets.plain
        }
        VERSION_PAINT.textAlign = Paint.Align.RIGHT
        VERSION_PAINT.textSize = VERSION_TEXT_SIZE
        VERSION_PAINT.color = Color.argb(170, 255, 255, 255)
        g.drawString(
            "v${BuildConfig.VERSION_NAME}",
            VERSION_X,
            VERSION_Y,
            VERSION_PAINT.color,
            VERSION_PAINT
        )
    }

    /** @deprecated Use [paint] — version label uses its own paint. */
    @JvmStatic
    @Deprecated("Use paint(Graphics)", ReplaceWith("paint(g)"))
    fun paint(g: Graphics, paint: Paint) {
        paint(g)
    }
}
