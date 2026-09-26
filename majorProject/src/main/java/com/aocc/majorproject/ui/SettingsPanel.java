package com.aocc.majorproject.ui;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;
import com.aocc.framework.Input;
import com.aocc.framework.implementation.RotationHandler;
import com.aocc.majorproject.Assets;
import com.aocc.majorproject.Button;
import com.aocc.majorproject.GameSettings;
import com.aocc.majorproject.MainMenuScreen;
import com.aocc.majorproject.Player;
import com.aocc.framework.PersonalMethods;

/**
 * Settings panel rendered on canvas in world coordinates.
 *
 * Geometry (world pixels, 1280×720):
 *   Outer panel  : centred, y=150, 900×440
 *   Sound column : left side, 180px wide
 *   Tilt column  : right side, remaining width
 *   Tilt buttons : 64×64 icons, 126px row spacing (fits 3 rows in column)
 *
 * Touch events are forwarded via {@link #handleTouch}.
 */
public class SettingsPanel {

    public static final int PANEL_WIDTH = 900;
    public static final int PANEL_HEIGHT = 440;
    public static final int PANEL_X = UiLayout.centerX(PANEL_WIDTH);
    public static final int PANEL_Y = 150;

    public static final int SOUND_COLUMN_WIDTH = 180;
    public static final int COLUMN_GAP = 32;
    public static final int ICON_SIZE = 100;
    public static final int ICON_GAP = 48;

    private static final int INNER_PADDING = 24;
    // Reduced from 150 → 126 so three tilt rows fit inside the column height.
    private static final int TILT_BUTTON_SPACING = 126;

    private final UiPanel outerPanel;
    private final UiPanel soundPanel;
    private final UiPanel tiltPanel;
    private final Button flatTiltButton;
    private final Button tiltedTiltButton;
    private final Button customTiltButton;

    private final int soundIconX;
    private final int soundIconY;
    private final int musicIconY;

    public SettingsPanel() {
        outerPanel = new UiPanel(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT,
                Color.argb(180, 0, 0, 0));

        int soundPanelX = PANEL_X + INNER_PADDING;
        int columnY = PANEL_Y + INNER_PADDING + 20;
        int columnHeight = PANEL_HEIGHT - INNER_PADDING * 2 - 20;
        soundPanel = new UiPanel(soundPanelX, columnY, SOUND_COLUMN_WIDTH, columnHeight,
                Color.DKGRAY);

        int tiltPanelX = soundPanelX + SOUND_COLUMN_WIDTH + COLUMN_GAP;
        int tiltPanelWidth = PANEL_WIDTH - INNER_PADDING * 2 - SOUND_COLUMN_WIDTH - COLUMN_GAP;
        tiltPanel = new UiPanel(tiltPanelX, columnY, tiltPanelWidth, columnHeight, Color.DKGRAY);

        int tiltButtonX = tiltPanelX + 36;
        int tiltButtonY = columnY + 56;
        flatTiltButton   = new Button(tiltButtonX, tiltButtonY,                         3, 0, "Flat");
        tiltedTiltButton = new Button(tiltButtonX, tiltButtonY + TILT_BUTTON_SPACING,   3, 0, "Tilted");
        customTiltButton = new Button(tiltButtonX, tiltButtonY + TILT_BUTTON_SPACING * 2, 3, 0, "Custom");

        soundIconX = soundPanelX + (SOUND_COLUMN_WIDTH - ICON_SIZE) / 2;
        soundIconY = columnY + 72;
        musicIconY = soundIconY + ICON_SIZE + ICON_GAP;
    }

    public UiBounds getOuterBounds() {
        return outerPanel.getBounds();
    }

    public UiBounds getSoundIconBounds() {
        return new UiBounds(soundIconX, soundIconY, ICON_SIZE, ICON_SIZE);
    }

    public UiBounds getMusicIconBounds() {
        return new UiBounds(soundIconX, musicIconY, ICON_SIZE, ICON_SIZE);
    }

    public int getMusicIconY() { return musicIconY; }
    public int getSoundIconX() { return soundIconX; }
    public int getSoundIconY() { return soundIconY; }

    public void paint(Graphics g, Paint paint, Player player) {
        outerPanel.paintBackground(g);
        outerPanel.paintTitle(g, paint, "Settings");

        soundPanel.paintBackground(g);
        soundPanel.paintTitle(g, paint, "Sound");

        tiltPanel.paintBackground(g);
        tiltPanel.paintTitle(g, paint, "Tilt Options");

        if (MainMenuScreen.sound) {
            g.drawImage(Assets.sound, soundIconX, soundIconY);
        } else {
            g.drawImage(Assets.sound_muted, soundIconX, soundIconY);
        }

        if (MainMenuScreen.music) {
            g.drawImage(Assets.music, soundIconX, musicIconY);
        } else {
            g.drawImage(Assets.music_muted, soundIconX, musicIconY);
        }

        flatTiltButton.paint(g, paint, player);
        tiltedTiltButton.paint(g, paint, player);
        customTiltButton.paint(g, paint, player);
    }

    /** @return true if the touch was consumed by the settings panel. */
    public boolean handleTouch(Input.TouchEvent event, Player player) {
        if (event.type != Input.TouchEvent.TOUCH_UP) {
            return false;
        }

        if (flatTiltButton.touchInBounds(event)) {
            GameSettings.applyFlatTilt(player);
            return true;
        }
        if (tiltedTiltButton.touchInBounds(event)) {
            GameSettings.applyTiltedTilt(player);
            return true;
        }
        if (customTiltButton.touchInBounds(event)) {
            GameSettings.applyCustomTilt(player);
            return true;
        }

        UiBounds soundBounds = getSoundIconBounds();
        if (PersonalMethods.touchInBounds(event, soundBounds.x, soundBounds.y,
                soundBounds.width, soundBounds.height)) {
            GameSettings.toggleSound();
            return true;
        }

        UiBounds musicBounds = getMusicIconBounds();
        if (PersonalMethods.touchInBounds(event, musicBounds.x, musicBounds.y,
                musicBounds.width, musicBounds.height)) {
            GameSettings.toggleMusic();
            return true;
        }

        return outerPanel.getBounds().contains(event)
                || soundPanel.getBounds().contains(event)
                || tiltPanel.getBounds().contains(event);
    }
}
