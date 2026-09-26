package com.aocc.majorproject.ui.compose

import com.aocc.framework.GameConstants
import com.aocc.majorproject.ui.UiBounds
import com.aocc.majorproject.ui.UiLayout

/** Original settings panel geometry, with tilt rows spaced to fit inside the panel. */
object SettingsLayout {
    const val PANEL_WIDTH = 900
    const val PANEL_HEIGHT = 440
    val PANEL_X: Int = UiLayout.centerX(PANEL_WIDTH)
    const val PANEL_Y = 150

    const val SOUND_COLUMN_WIDTH = 180
    const val COLUMN_GAP = 32
    const val ICON_SIZE = 100
    const val ICON_GAP = 48

    private const val INNER_PADDING = 24
    private const val TITLE_GAP = 30
    private const val TITLE_TEXT_SIZE = 30f
    private const val PROMPT_TEXT_SIZE = 50f

    // 64px icons + 62px gaps keeps the third row inside the tilt column.
    private const val TILT_ICON_SIZE = 64
    private const val TILT_BUTTON_SPACING = 126

    val outerPanel: UiBounds
        get() = UiBounds(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT)

    val soundPanel: UiBounds
        get() {
            val x = PANEL_X + INNER_PADDING
            val y = columnY
            val height = PANEL_HEIGHT - INNER_PADDING * 2 - 20
            return UiBounds(x, y, SOUND_COLUMN_WIDTH, height)
        }

    val tiltPanel: UiBounds
        get() {
            val sound = soundPanel
            val x = sound.x + SOUND_COLUMN_WIDTH + COLUMN_GAP
            val width = PANEL_WIDTH - INNER_PADDING * 2 - SOUND_COLUMN_WIDTH - COLUMN_GAP
            return UiBounds(x, sound.y, width, sound.height)
        }

    val soundIcon: UiBounds
        get() {
            val panel = soundPanel
            val x = panel.x + (SOUND_COLUMN_WIDTH - ICON_SIZE) / 2
            val y = panel.y + 72
            return UiBounds(x, y, ICON_SIZE, ICON_SIZE)
        }

    val musicIcon: UiBounds
        get() {
            val sound = soundIcon
            return UiBounds(sound.x, sound.y + ICON_SIZE + ICON_GAP, ICON_SIZE, ICON_SIZE)
        }

    val flatTiltButton: UiBounds
        get() = tiltButtonAt(0)

    val tiltedTiltButton: UiBounds
        get() = tiltButtonAt(1)

    val customTiltButton: UiBounds
        get() = tiltButtonAt(2)

    val menuButton: UiBounds
        get() = UiBounds(0, 0, 200, 70)

    val promptCenterY: Int
        get() = PANEL_Y + PANEL_HEIGHT + 50

    val settingsTitleCenterY: Int
        get() = PANEL_Y - TITLE_GAP

    val soundTitleCenterY: Int
        get() = soundPanel.y - TITLE_GAP

    val tiltTitleCenterY: Int
        get() = tiltPanel.y - TITLE_GAP

    const val titleTextSize = TITLE_TEXT_SIZE
    const val promptTextSize = PROMPT_TEXT_SIZE
    const val tiltLabelOffsetX = 12
    const val tiltIconSize = TILT_ICON_SIZE

    private val columnY: Int
        get() = PANEL_Y + INNER_PADDING + 20

    private fun tiltButtonAt(index: Int): UiBounds {
        val panel = tiltPanel
        val x = panel.x + 36
        val y = panel.y + 56 + index * TILT_BUTTON_SPACING
        return UiBounds(x, y, TILT_ICON_SIZE, TILT_ICON_SIZE)
    }
}
