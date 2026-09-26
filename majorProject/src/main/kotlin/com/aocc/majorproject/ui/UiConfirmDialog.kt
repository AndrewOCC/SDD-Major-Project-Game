package com.aocc.majorproject.ui

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics

/** Small centred "are you sure?" panel with two buttons, shown in place of a caller's own UI. */
class UiConfirmDialog(
    private val message: String,
    confirmLabel: String,
    cancelLabel: String,
) {

    private val panel = UiPanel(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT, Color.argb(220, 20, 20, 20))
    private val confirmButton: UiButton
    private val cancelButton: UiButton

    init {
        val buttonY = PANEL_Y + PANEL_HEIGHT - BUTTON_HEIGHT - 36
        val groupWidth = BUTTON_WIDTH * 2 + BUTTON_GAP
        val groupLeft = panel.getBounds().centerX() - groupWidth / 2
        confirmButton = UiButton(groupLeft, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, confirmLabel)
        cancelButton = UiButton(groupLeft + BUTTON_WIDTH + BUTTON_GAP, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, cancelLabel)
    }

    fun getBounds(): UiBounds = panel.getBounds()

    fun getConfirmBounds(): UiBounds = confirmButton.getBounds()

    fun getCancelBounds(): UiBounds = cancelButton.getBounds()

    fun paint(g: Graphics, paint: Paint) {
        panel.paintBackground(g)
        val previousSize = paint.textSize
        paint.textSize = MESSAGE_TEXT_SIZE
        UiText.drawCentered(g, paint, message, panel.getBounds().centerX(), PANEL_Y + 70, Color.WHITE)
        paint.textSize = previousSize
        confirmButton.paint(g)
        cancelButton.paint(g)
    }

    companion object {
        private const val PANEL_WIDTH = 620
        private const val PANEL_HEIGHT = 260
        val PANEL_X: Int = UiLayout.centerX(PANEL_WIDTH)
        const val PANEL_Y = 230
        private const val BUTTON_WIDTH = 220
        private const val BUTTON_HEIGHT = 80
        private const val BUTTON_GAP = 32
        private const val MESSAGE_TEXT_SIZE = 36f
    }
}
