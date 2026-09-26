package com.aocc.majorproject.ui;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;
import com.aocc.framework.Input;
import com.aocc.majorproject.Assets;
import com.aocc.majorproject.Button;
import com.aocc.majorproject.GamePreferences;
import com.aocc.majorproject.GameSettings;
import com.aocc.majorproject.MajorProjectGame;
import com.aocc.majorproject.Player;
import com.aocc.majorproject.input.GamepadInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings panel rendered on canvas in world coordinates.
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
    private static final int TILT_BUTTON_SPACING = 126;
    public static final int ITEM_COUNT = 6;

    private final UiPanel outerPanel;
    private final UiPanel soundPanel;
    private final UiPanel tiltPanel;
    private final Button flatTiltButton;
    private final Button tiltedTiltButton;
    private final Button customTiltButton;

    private final int soundIconX;
    private final int soundIconY;
    private final int musicIconY;
    private final UiBounds displayToggleBounds;

    private int selectedIndex = 0;
    private MajorProjectGame game;

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
        int displayY = musicIconY + ICON_SIZE + 20;
        displayToggleBounds = new UiBounds(soundPanelX + 8, displayY,
                SOUND_COLUMN_WIDTH - 16, 56);
    }

    public void setGame(MajorProjectGame game) {
        this.game = game;
    }

    public UiBounds getOuterBounds() {
        return outerPanel.getBounds();
    }

    public void paint(Graphics g, Paint paint, Player player) {
        paint(g, paint, player, selectedIndex);
    }

    /** @param focusedItemIndex settings item to highlight, or {@code -1} to skip highlight */
    public void paint(Graphics g, Paint paint, Player player, int focusedItemIndex) {
        outerPanel.paintBackground(g);
        outerPanel.paintTitle(g, paint, "Settings");

        soundPanel.paintBackground(g);
        soundPanel.paintTitle(g, paint, "Sound");

        tiltPanel.paintBackground(g);
        tiltPanel.paintTitle(g, paint, "Tilt Options");

        if (GamePreferences.sound) {
            g.drawImage(Assets.sound, soundIconX, soundIconY);
        } else {
            g.drawImage(Assets.sound_muted, soundIconX, soundIconY);
        }

        if (GamePreferences.music) {
            g.drawImage(Assets.music, soundIconX, musicIconY);
        } else {
            g.drawImage(Assets.music_muted, soundIconX, musicIconY);
        }

        paintDisplayToggle(g, paint);

        flatTiltButton.paint(g, paint, player);
        tiltedTiltButton.paint(g, paint, player);
        customTiltButton.paint(g, paint, player);

        if (focusedItemIndex >= 0) {
            paintSelectionHighlight(g, focusedItemIndex);
        }
    }

    private void paintDisplayToggle(Graphics g, Paint paint) {
        g.drawRect(displayToggleBounds.x, displayToggleBounds.y,
                displayToggleBounds.width, displayToggleBounds.height, Color.rgb(60, 60, 60));
        String label = GamePreferences.secondScreenEnabled ? "2nd Screen: ON" : "2nd Screen: OFF";
        UiText.drawInBounds(g, paint, label, displayToggleBounds,
                UiText.HAlign.CENTER, Color.WHITE);
    }

    private void paintSelectionHighlight(Graphics g, int index) {
        UiBounds bounds = boundsForIndex(index);
        if (bounds == null) {
            return;
        }
        if (index >= 2 && index <= 4) {
            UiSelectionHighlight.paintCircle(g, bounds.centerX(), bounds.centerY(),
                    bounds.width / 2);
            return;
        }
        UiSelectionHighlight.paintRect(g, bounds);
    }

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

        if (getSoundIconBoundsInternal().contains(event)) {
            GameSettings.toggleSound();
            return true;
        }
        if (getMusicIconBoundsInternal().contains(event)) {
            GameSettings.toggleMusic();
            return true;
        }
        if (displayToggleBounds.contains(event)) {
            GameSettings.toggleSecondScreen(game);
            return true;
        }

        return outerPanel.getBounds().contains(event)
                || soundPanel.getBounds().contains(event)
                || tiltPanel.getBounds().contains(event);
    }

    public boolean handleGamepad(GamepadInput.Action action, Player player) {
        SpatialFocusNavigator.Direction direction = SpatialFocusNavigator.directionFrom(action);
        if (direction != null) {
            selectedIndex = SpatialFocusNavigator.findNext(
                    selectedIndex, direction, buildFocusBoundsList());
            return true;
        }
        if (action == GamepadInput.Action.CONFIRM) {
            activateSelected(player);
            return true;
        }
        return false;
    }

    public UiBounds getItemBounds(int index) {
        return boundsForIndex(index);
    }

    public void activateFocusIndex(int index, Player player) {
        switch (index) {
            case 0 -> GameSettings.toggleSound();
            case 1 -> GameSettings.toggleMusic();
            case 2 -> GameSettings.applyFlatTilt(player);
            case 3 -> GameSettings.applyTiltedTilt(player);
            case 4 -> GameSettings.applyCustomTilt(player);
            case 5 -> GameSettings.toggleSecondScreen(game);
            default -> { }
        }
    }

    private List<UiBounds> buildFocusBoundsList() {
        List<UiBounds> items = new ArrayList<>(ITEM_COUNT);
        for (int i = 0; i < ITEM_COUNT; i++) {
            items.add(boundsForIndex(i));
        }
        return items;
    }

    private void activateSelected(Player player) {
        activateFocusIndex(selectedIndex, player);
    }

    int getSoundIconY() {
        return soundIconY;
    }

    int getMusicIconY() {
        return musicIconY;
    }

    UiBounds getSoundIconBounds() {
        return new UiBounds(soundIconX, soundIconY, ICON_SIZE, ICON_SIZE);
    }

    UiBounds getMusicIconBounds() {
        return new UiBounds(soundIconX, musicIconY, ICON_SIZE, ICON_SIZE);
    }

    private UiBounds getSoundIconBoundsInternal() {
        return new UiBounds(soundIconX, soundIconY, ICON_SIZE, ICON_SIZE);
    }

    private UiBounds getMusicIconBoundsInternal() {
        return new UiBounds(soundIconX, musicIconY, ICON_SIZE, ICON_SIZE);
    }

    private UiBounds boundsForIndex(int index) {
        return switch (index) {
            case 0 -> getSoundIconBoundsInternal();
            case 1 -> getMusicIconBoundsInternal();
            case 2 -> flatTiltButton.getBounds();
            case 3 -> tiltedTiltButton.getBounds();
            case 4 -> customTiltButton.getBounds();
            case 5 -> displayToggleBounds;
            default -> null;
        };
    }
}
