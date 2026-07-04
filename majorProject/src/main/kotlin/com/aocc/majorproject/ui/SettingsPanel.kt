package com.aocc.majorproject.ui

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics
import com.aocc.framework.Input
import com.aocc.majorproject.Assets
import com.aocc.majorproject.Button
import com.aocc.majorproject.GamePreferences
import com.aocc.majorproject.GameSettings
import com.aocc.majorproject.MajorProjectGame
import com.aocc.majorproject.Player
import com.aocc.majorproject.input.GamepadInput

/**
 * Settings panel rendered on canvas in world coordinates.
 */
class SettingsPanel {

    private val outerPanel: UiPanel
    private val soundPanel: UiPanel
    private val tiltPanel: UiPanel
    private val flatTiltButton: Button
    private val tiltedTiltButton: Button
    private val customTiltButton: Button

    private val soundIconX: Int
    private val soundIconY: Int
    private val musicIconY: Int
    private val displayToggleBounds: UiBounds

    private var selectedIndex = 0
    private var game: MajorProjectGame? = null

    init {
        outerPanel = UiPanel(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT,
            Color.argb(180, 0, 0, 0))

        val soundPanelX = PANEL_X + INNER_PADDING
        val columnY = PANEL_Y + INNER_PADDING + 20
        val columnHeight = PANEL_HEIGHT - INNER_PADDING * 2 - 20
        soundPanel = UiPanel(soundPanelX, columnY, SOUND_COLUMN_WIDTH, columnHeight, Color.DKGRAY)

        val tiltPanelX = soundPanelX + SOUND_COLUMN_WIDTH + COLUMN_GAP
        val tiltPanelWidth = PANEL_WIDTH - INNER_PADDING * 2 - SOUND_COLUMN_WIDTH - COLUMN_GAP
        tiltPanel = UiPanel(tiltPanelX, columnY, tiltPanelWidth, columnHeight, Color.DKGRAY)

        val tiltButtonX = tiltPanelX + 36
        val tiltButtonY = columnY + 56
        flatTiltButton = Button(tiltButtonX, tiltButtonY, 3, 0, "Flat")
        tiltedTiltButton = Button(tiltButtonX, tiltButtonY + TILT_BUTTON_SPACING, 3, 0, "Tilted")
        customTiltButton = Button(tiltButtonX, tiltButtonY + TILT_BUTTON_SPACING * 2, 3, 0, "Custom")

        soundIconX = soundPanelX + (SOUND_COLUMN_WIDTH - ICON_SIZE) / 2
        soundIconY = columnY + 72
        musicIconY = soundIconY + ICON_SIZE + ICON_GAP
        val displayY = musicIconY + ICON_SIZE + 20
        displayToggleBounds = UiBounds(soundPanelX + 8, displayY, SOUND_COLUMN_WIDTH - 16, 56)
    }

    fun setGame(game: MajorProjectGame) {
        this.game = game
    }

    fun getOuterBounds(): UiBounds = outerPanel.getBounds()

    fun paint(g: Graphics, paint: Paint, player: Player) {
        paint(g, paint, player, selectedIndex)
    }

    /** @param focusedItemIndex settings item to highlight, or `-1` to skip highlight */
    fun paint(g: Graphics, paint: Paint, player: Player, focusedItemIndex: Int) {
        outerPanel.paintBackground(g)
        outerPanel.paintTitle(g, paint, "Settings")

        soundPanel.paintBackground(g)
        soundPanel.paintTitle(g, paint, "Sound")

        tiltPanel.paintBackground(g)
        tiltPanel.paintTitle(g, paint, "Tilt Options")

        val soundIcon = if (GamePreferences.sound) Assets.sound else Assets.sound_muted
        soundIcon?.let { g.drawImage(it, soundIconX, soundIconY) }

        val musicIcon = if (GamePreferences.music) Assets.music else Assets.music_muted
        musicIcon?.let { g.drawImage(it, soundIconX, musicIconY) }

        paintDisplayToggle(g, paint)

        flatTiltButton.paint(g, paint, player)
        tiltedTiltButton.paint(g, paint, player)
        customTiltButton.paint(g, paint, player)

        if (focusedItemIndex >= 0) {
            paintSelectionHighlight(g, focusedItemIndex)
        }
    }

    private fun paintDisplayToggle(g: Graphics, paint: Paint) {
        g.drawRect(
            displayToggleBounds.x, displayToggleBounds.y,
            displayToggleBounds.width, displayToggleBounds.height,
            Color.rgb(60, 60, 60)
        )
        val label = if (GamePreferences.secondScreenEnabled) "2nd Screen: ON" else "2nd Screen: OFF"
        UiText.drawInBounds(g, paint, label, displayToggleBounds, UiText.HAlign.CENTER, Color.WHITE)
    }

    private fun paintSelectionHighlight(g: Graphics, index: Int) {
        val bounds = boundsForIndex(index) ?: return
        if (index in 2..4) {
            UiSelectionHighlight.paintCircle(g, bounds.centerX(), bounds.centerY(), bounds.width / 2)
            return
        }
        UiSelectionHighlight.paintRect(g, bounds)
    }

    fun handleTouch(event: Input.TouchEvent, player: Player): Boolean {
        if (event.type != Input.TouchEvent.TOUCH_UP) {
            return false
        }

        if (flatTiltButton.touchInBounds(event)) {
            GameSettings.applyFlatTilt(player)
            return true
        }
        if (tiltedTiltButton.touchInBounds(event)) {
            GameSettings.applyTiltedTilt(player)
            return true
        }
        if (customTiltButton.touchInBounds(event)) {
            GameSettings.applyCustomTilt(player)
            return true
        }

        if (getSoundIconBoundsInternal().contains(event)) {
            GameSettings.toggleSound()
            return true
        }
        if (getMusicIconBoundsInternal().contains(event)) {
            GameSettings.toggleMusic()
            return true
        }
        if (displayToggleBounds.contains(event)) {
            GameSettings.toggleSecondScreen(game)
            return true
        }

        return outerPanel.getBounds().contains(event)
            || soundPanel.getBounds().contains(event)
            || tiltPanel.getBounds().contains(event)
    }

    fun handleGamepad(action: GamepadInput.Action, player: Player): Boolean {
        val direction = SpatialFocusNavigator.directionFrom(action)
        if (direction != null) {
            selectedIndex = SpatialFocusNavigator.findNext(
                selectedIndex, direction, buildFocusBoundsList()
            )
            return true
        }
        if (action == GamepadInput.Action.CONFIRM) {
            activateSelected(player)
            return true
        }
        return false
    }

    fun getItemBounds(index: Int): UiBounds? = boundsForIndex(index)

    fun activateFocusIndex(index: Int, player: Player) {
        when (index) {
            0 -> GameSettings.toggleSound()
            1 -> GameSettings.toggleMusic()
            2 -> GameSettings.applyFlatTilt(player)
            3 -> GameSettings.applyTiltedTilt(player)
            4 -> GameSettings.applyCustomTilt(player)
            5 -> GameSettings.toggleSecondScreen(game)
        }
    }

    private fun buildFocusBoundsList(): List<UiBounds> {
        return (0 until ITEM_COUNT).mapNotNull { boundsForIndex(it) }
    }

    private fun activateSelected(player: Player) {
        activateFocusIndex(selectedIndex, player)
    }

    internal fun getSoundIconY(): Int = soundIconY

    internal fun getMusicIconY(): Int = musicIconY

    internal fun getSoundIconBounds(): UiBounds =
        UiBounds(soundIconX, soundIconY, ICON_SIZE, ICON_SIZE)

    internal fun getMusicIconBounds(): UiBounds =
        UiBounds(soundIconX, musicIconY, ICON_SIZE, ICON_SIZE)

    private fun getSoundIconBoundsInternal(): UiBounds =
        UiBounds(soundIconX, soundIconY, ICON_SIZE, ICON_SIZE)

    private fun getMusicIconBoundsInternal(): UiBounds =
        UiBounds(soundIconX, musicIconY, ICON_SIZE, ICON_SIZE)

    private fun boundsForIndex(index: Int): UiBounds? {
        return when (index) {
            0 -> getSoundIconBoundsInternal()
            1 -> getMusicIconBoundsInternal()
            2 -> flatTiltButton.getBounds()
            3 -> tiltedTiltButton.getBounds()
            4 -> customTiltButton.getBounds()
            5 -> displayToggleBounds
            else -> null
        }
    }

    companion object {
        const val PANEL_WIDTH = 900
        const val PANEL_HEIGHT = 440
        val PANEL_X: Int = UiLayout.centerX(PANEL_WIDTH)
        const val PANEL_Y = 150

        const val SOUND_COLUMN_WIDTH = 180
        const val COLUMN_GAP = 32
        const val ICON_SIZE = 100
        const val ICON_GAP = 48

        private const val INNER_PADDING = 24
        private const val TILT_BUTTON_SPACING = 126
        const val ITEM_COUNT = 6
    }
}
