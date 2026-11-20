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
    private var backgroundColor: Int = UiStyle.PANEL
    private var pressedBackgroundColor: Int = UiStyle.ACCENT_PRESSED
    private var textColor: Int = UiStyle.TEXT
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
        g.drawRectOutline(bounds.x, bounds.y, bounds.width, bounds.height, UiStyle.RULE_SOFT, 1.5f)
        // Mustard underline: the one accent on an otherwise flat button.
        g.drawRect(bounds.x, bounds.y + bounds.height - 4, bounds.width, 4, UiStyle.ACCENT)

        if (Assets.plain != null) {
            labelPaint.typeface = Assets.plain
        }
        labelPaint.letterSpacing = UiStyle.LETTER_SPACING
        // Inter is wider than the old pixel font; keep labels comfortably inside.
        labelPaint.textSize = textSize * TEXT_SCALE
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
        private const val TEXT_SCALE = 0.72f

        @JvmStatic
        fun menuAt(x: Int, y: Int): UiButton {
            return UiButton(x, y, MENU_WIDTH, MENU_HEIGHT, "Menu")
        }
    }
}
