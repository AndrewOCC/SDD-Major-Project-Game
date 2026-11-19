package com.aocc.majorproject.ui

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics

/** Small centred "are you sure?" panel with two buttons, shown in place of a caller's own UI. */
class UiConfirmDialog(
    private val message: String,
    confirmLabel: String,
    cancelLabel: String,
    worldWidth: Int = GameConstants.WORLD_WIDTH,
    private val panelY: Int = PANEL_Y,
) {

    private val panel = UiPanel(
        (worldWidth - PANEL_WIDTH) / 2, panelY, PANEL_WIDTH, PANEL_HEIGHT, Color.argb(240, 44, 44, 50)
    )
    private val confirmButton: UiButton
    private val cancelButton: UiButton

    init {
        val buttonY = panelY + PANEL_HEIGHT - BUTTON_HEIGHT - 36
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
        val bounds = panel.getBounds()
        g.drawRectOutline(bounds.x, bounds.y, bounds.width, bounds.height, Color.argb(160, 255, 255, 255), 2f)
        val previousSize = paint.textSize
        paint.textSize = MESSAGE_TEXT_SIZE
        UiText.drawCentered(g, paint, message, panel.getBounds().centerX(), panelY + 70, Color.WHITE)
        paint.textSize = previousSize
        confirmButton.paint(g)
        cancelButton.paint(g)
    }

    companion object {
        private const val PANEL_WIDTH = 620
        private const val PANEL_HEIGHT = 260
        const val PANEL_Y = 230
        private const val BUTTON_WIDTH = 220
        private const val BUTTON_HEIGHT = 80
        private const val BUTTON_GAP = 32
        private const val MESSAGE_TEXT_SIZE = 36f
    }
}
