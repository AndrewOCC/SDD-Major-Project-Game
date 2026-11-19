package com.aocc.majorproject.ui

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics
import com.aocc.framework.Input
import com.aocc.majorproject.Assets

/** Rectangular label button with centered text and optional pressed state. */
class UiButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private var label: String,
    private val textSize: Float = MENU_TEXT_SIZE,
) {

    private val bounds = UiBounds(x, y, width, height)
    private var backgroundColor: Int = Color.DKGRAY
    private var pressedBackgroundColor: Int = Color.rgb(195, 195, 195)
    private var textColor: Int = Color.WHITE
    private var pressed = false
    // Per instance: the top and rear displays paint on different threads.
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(x: Int, y: Int, width: Int, height: Int, label: String) :
        this(x, y, width, height, label, MENU_TEXT_SIZE)

    fun setPressed(pressed: Boolean) {
        this.pressed = pressed
    }

    fun paint(g: Graphics) {
        val bg = if (pressed) pressedBackgroundColor else backgroundColor
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, bg)

        if (Assets.plain != null) {
            labelPaint.typeface = Assets.plain
        }
        labelPaint.textSize = textSize
        labelPaint.color = textColor
        UiText.drawInBounds(g, labelPaint, label, bounds, UiText.HAlign.CENTER, textColor)
    }

    /** @deprecated Use [paint] — label rendering no longer shares caller paint. */
    @Deprecated("Use paint(Graphics)", ReplaceWith("paint(g)"))
    fun paint(g: Graphics, paint: Paint) {
        paint(g)
    }

    fun touchInBounds(event: Input.TouchEvent): Boolean {
        return bounds.contains(event)
    }

    fun getBounds(): UiBounds = bounds

    fun getLabel(): String = label

    fun setLabel(label: String) {
        this.label = label
    }

    companion object {
        const val MENU_WIDTH = 200
        const val MENU_HEIGHT = 70
        const val MENU_TEXT_SIZE = 50f

        @JvmStatic
        fun menuAt(x: Int, y: Int): UiButton {
            return UiButton(x, y, MENU_WIDTH, MENU_HEIGHT, "Menu")
        }
    }
}
