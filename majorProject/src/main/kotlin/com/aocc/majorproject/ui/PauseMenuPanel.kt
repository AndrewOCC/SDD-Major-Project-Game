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
import kotlin.math.ceil

/**
 * Pause-menu layout: a left column (Sound, Music, 2nd-Screen) and a right column
 * (Flat, Tilted, Custom tilt) sharing the same item size/spacing for visual symmetry,
 * with big Resume / Quit buttons in the middle.
 */
class PauseMenuPanel {

    enum class Item {
        SOUND, MUSIC, SECOND_SCREEN, RESUME, QUIT, TILT_FLAT, TILT_TILTED, TILT_CUSTOM
    }

    private val outerPanel = UiPanel(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT, Color.argb(180, 0, 0, 0))
    private val soundPanel: UiPanel
    private val tiltPanel: UiPanel

    private val soundIconX: Int
    private val soundIconY: Int
    private val musicIconY: Int
    private val secondScreenY: Int
    private val secondScreenBounds: UiBounds

    private val tiltButtonX: Int
    private val flatTiltButton: Button
    private val tiltedTiltButton: Button
    private val customTiltButton: Button

    private val resumeButton: UiButton
    private val quitButton: UiButton

    private var game: MajorProjectGame? = null

    init {
        val columnY = PANEL_Y + INNER_PADDING + 20
        val columnHeight = PANEL_HEIGHT - INNER_PADDING * 2 - 20

        soundPanel = UiPanel(PANEL_X, columnY, COLUMN_WIDTH, columnHeight, Color.DKGRAY)
        val tiltPanelX = PANEL_X + PANEL_WIDTH - COLUMN_WIDTH
        tiltPanel = UiPanel(tiltPanelX, columnY, COLUMN_WIDTH, columnHeight, Color.DKGRAY)

        soundIconX = PANEL_X + (COLUMN_WIDTH - ICON_SIZE) / 2
        soundIconY = columnY + ITEM_TOP_OFFSET
        musicIconY = soundIconY + ITEM_PITCH
        secondScreenY = musicIconY + ITEM_PITCH
        secondScreenBounds = UiBounds(soundIconX, secondScreenY, ICON_SIZE, ICON_SIZE)

        // Same item size/spacing as the sound column for symmetry.
        tiltButtonX = tiltPanelX + (COLUMN_WIDTH - TILT_ICON_WIDTH) / 2
        flatTiltButton = Button(tiltButtonX, soundIconY, 3, 0, "Flat")
        tiltedTiltButton = Button(tiltButtonX, musicIconY, 3, 0, "Tilted")
        customTiltButton = Button(tiltButtonX, secondScreenY, 3, 0, "Custom")

        val middleX = PANEL_X + COLUMN_WIDTH + COLUMN_GAP
        val middleWidth = PANEL_WIDTH - COLUMN_WIDTH * 2 - COLUMN_GAP * 2
        resumeButton = UiButton(
            middleX, columnY + MIDDLE_BUTTON_TOP, middleWidth, MIDDLE_BUTTON_HEIGHT,
            "Resume", MIDDLE_BUTTON_TEXT_SIZE
        )
        quitButton = UiButton(
            middleX, columnY + MIDDLE_BUTTON_TOP + MIDDLE_BUTTON_HEIGHT + MIDDLE_BUTTON_GAP,
            middleWidth, MIDDLE_BUTTON_HEIGHT, "Quit", MIDDLE_BUTTON_TEXT_SIZE
        )
    }

    fun setGame(game: MajorProjectGame) {
        this.game = game
    }

    fun getOuterBounds(): UiBounds = outerPanel.getBounds()

    fun getResumeBounds(): UiBounds = resumeButton.getBounds()

    fun getQuitBounds(): UiBounds = quitButton.getBounds()

    fun getItemBounds(item: Item): UiBounds = when (item) {
        Item.SOUND -> UiBounds(soundIconX, soundIconY, ICON_SIZE, ICON_SIZE)
        Item.MUSIC -> UiBounds(soundIconX, musicIconY, ICON_SIZE, ICON_SIZE)
        Item.SECOND_SCREEN -> secondScreenBounds
        Item.RESUME -> resumeButton.getBounds()
        Item.QUIT -> quitButton.getBounds()
        Item.TILT_FLAT -> flatTiltButton.getBounds()
        Item.TILT_TILTED -> tiltedTiltButton.getBounds()
        Item.TILT_CUSTOM -> customTiltButton.getBounds()
    }

    /**
     * @param focusedItem gamepad-focused item to highlight, or `null` to skip highlight
     * @param resumeCountdownSeconds remaining resume countdown; the Resume button shows
     *   the whole-second count while this is `> 0`
     */
    fun paint(
        g: Graphics,
        paint: Paint,
        player: Player,
        focusedItem: Item?,
        resumeCountdownSeconds: Float,
    ) {
        outerPanel.paintBackground(g)
        outerPanel.paintTitle(g, paint, "Paused")

        soundPanel.paintBackground(g)
        soundPanel.paintTitle(g, paint, "Sound")

        tiltPanel.paintBackground(g)
        tiltPanel.paintTitle(g, paint, "Tilt Options")

        val soundIcon = if (GamePreferences.sound) Assets.sound else Assets.sound_muted
        soundIcon?.let { g.drawImage(it, soundIconX, soundIconY) }

        val musicIcon = if (GamePreferences.music) Assets.music else Assets.music_muted
        musicIcon?.let { g.drawImage(it, soundIconX, musicIconY) }

        paintSecondScreenButton(g, paint)

        flatTiltButton.paint(g, paint, player)
        tiltedTiltButton.paint(g, paint, player)
        customTiltButton.paint(g, paint, player)

        resumeButton.setLabel(
            if (resumeCountdownSeconds > 0f) {
                ceil(resumeCountdownSeconds).toInt().coerceAtLeast(1).toString()
            } else {
                "Resume"
            }
        )
        resumeButton.paint(g)
        quitButton.paint(g)

        if (focusedItem != null) {
            paintFocusHighlight(g, focusedItem)
        }
    }

    private fun paintSecondScreenButton(g: Graphics, paint: Paint) {
        val centerX = secondScreenBounds.centerX().toFloat()
        val centerY = secondScreenBounds.centerY().toFloat()
        val radius = secondScreenBounds.width / 2f
        val enabled = GamePreferences.secondScreenEnabled
        g.drawCircle(centerX, centerY, radius, if (enabled) Color.argb(220, 0, 90, 220) else Color.DKGRAY)
        g.drawCircleOutline(centerX, centerY, radius, Color.WHITE, 3f)

        // Small monitor glyph: screen outline + stand.
        val screenWidth = radius * 1.1f
        val screenHeight = radius * 0.75f
        g.drawRectOutline(
            (centerX - screenWidth / 2f).toInt(), (centerY - screenHeight / 2f - radius * 0.15f).toInt(),
            screenWidth.toInt(), screenHeight.toInt(), Color.WHITE, 2.5f
        )
        val standTopY = centerY - screenHeight / 2f - radius * 0.15f + screenHeight
        g.drawLine(
            centerX.toInt(), standTopY.toInt(),
            centerX.toInt(), (standTopY + radius * 0.22f).toInt(), Color.WHITE
        )

        val previousSize = paint.textSize
        paint.textSize = SECOND_SCREEN_LABEL_SIZE
        UiText.drawInBounds(
            g, paint, "2nd Screen",
            UiBounds(secondScreenBounds.x - 40, secondScreenBounds.y + secondScreenBounds.height + 4, ICON_SIZE + 80, 24),
            UiText.HAlign.CENTER, Color.WHITE
        )
        paint.textSize = previousSize
    }

    private fun paintFocusHighlight(g: Graphics, item: Item) {
        val bounds = getItemBounds(item)
        when (item) {
            Item.SECOND_SCREEN, Item.TILT_FLAT, Item.TILT_TILTED, Item.TILT_CUSTOM ->
                UiSelectionHighlight.paintCircle(g, bounds.centerX(), bounds.centerY(), bounds.width / 2)
            else -> UiSelectionHighlight.paintRect(g, bounds)
        }
    }

    /** Handles sound / music / tilt / 2nd-screen toggles. Resume and Quit are handled by the caller. */
    fun handleSettingsTouch(event: Input.TouchEvent, player: Player): Boolean {
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
        if (getItemBounds(Item.SOUND).contains(event)) {
            GameSettings.toggleSound()
            return true
        }
        if (getItemBounds(Item.MUSIC).contains(event)) {
            GameSettings.toggleMusic()
            return true
        }
        if (secondScreenBounds.contains(event)) {
            GameSettings.toggleSecondScreen(game)
            return true
        }
        return false
    }

    fun activateSettingsItem(item: Item, player: Player) {
        when (item) {
            Item.SOUND -> GameSettings.toggleSound()
            Item.MUSIC -> GameSettings.toggleMusic()
            Item.SECOND_SCREEN -> GameSettings.toggleSecondScreen(game)
            Item.TILT_FLAT -> GameSettings.applyFlatTilt(player)
            Item.TILT_TILTED -> GameSettings.applyTiltedTilt(player)
            Item.TILT_CUSTOM -> GameSettings.applyCustomTilt(player)
            Item.RESUME, Item.QUIT -> Unit // handled by the caller (countdown / confirm flow)
        }
    }

    companion object {
        const val PANEL_WIDTH = 900
        const val PANEL_HEIGHT = 440
        val PANEL_X: Int = UiLayout.centerX(PANEL_WIDTH)
        const val PANEL_Y = 150

        const val COLUMN_WIDTH = 180
        const val COLUMN_GAP = 32
        const val ICON_SIZE = 100

        /** Shared vertical pitch between the 3 sound-column and 3 tilt-column items. */
        const val ITEM_PITCH = 105
        private const val ITEM_TOP_OFFSET = 20

        private const val INNER_PADDING = 24
        private const val TILT_ICON_WIDTH = 64

        private const val MIDDLE_BUTTON_TOP = 20
        private const val MIDDLE_BUTTON_HEIGHT = 140
        private const val MIDDLE_BUTTON_GAP = 24
        private const val MIDDLE_BUTTON_TEXT_SIZE = 46f
        private const val SECOND_SCREEN_LABEL_SIZE = 22f

        /** All focusable pause-menu items, in a fixed order used for gamepad focus indices. */
        val FOCUS_ITEMS: List<Item> = Item.values().toList()
    }
}
