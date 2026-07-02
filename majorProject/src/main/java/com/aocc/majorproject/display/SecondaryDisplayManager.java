package com.aocc.majorproject.display;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;

import com.aocc.majorproject.Assets;
import com.aocc.majorproject.GamePreferences;
import com.aocc.majorproject.GameScreen;
import com.aocc.majorproject.MainMenuScreen;
import com.aocc.majorproject.MajorProjectGame;
import com.aocc.majorproject.TutorialScreen;
import com.aocc.framework.Screen;

/** Detects secondary displays and mirrors menu / pause content when enabled. */
public final class SecondaryDisplayManager implements DisplayManager.DisplayListener {

    private final MajorProjectGame activity;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final DisplayManager displayManager;

    private SecondaryDisplayPresentation presentation;
    private SecondaryDisplayMode currentMode = SecondaryDisplayMode.OFF;

    public SecondaryDisplayManager(MajorProjectGame activity) {
        this.activity = activity;
        this.displayManager =
                (DisplayManager) activity.getSystemService(Context.DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(this, mainHandler);
        }
    }

    public void shutdown() {
        if (displayManager != null) {
            displayManager.unregisterDisplayListener(this);
        }
        dismissPresentation();
    }

    public void updateForScreen(Screen screen) {
        if (screen instanceof MainMenuScreen) {
            show(SecondaryDisplayMode.MAIN_MENU, Assets.menu_bg, null);
        } else if (screen instanceof TutorialScreen) {
            show(SecondaryDisplayMode.MAIN_MENU, Assets.tutorial, null);
        } else if (screen instanceof GameScreen gameScreen) {
            updateForGameState(gameScreen.getState());
        } else {
            show(SecondaryDisplayMode.OFF, null, null);
        }
    }

    public void updateForGameState(GameScreen.GameState state) {
        switch (state) {
            case Ready:
            case Paused:
                show(SecondaryDisplayMode.PAUSE_MENU, Assets.game_bg, "Paused");
                break;
            case GameOver:
                show(SecondaryDisplayMode.PAUSE_MENU, Assets.game_bg, "Game Over");
                break;
            case Running:
                show(SecondaryDisplayMode.BACKGROUND, Assets.menu_bg, null);
                break;
            default:
                show(SecondaryDisplayMode.OFF, null, null);
                break;
        }
    }

    private void show(SecondaryDisplayMode mode, com.aocc.framework.Image background,
            String overlayLabel) {
        currentMode = mode;
        if (!GamePreferences.secondScreenEnabled || mode == SecondaryDisplayMode.OFF) {
            dismissPresentation();
            return;
        }

        Display secondary = findSecondaryDisplay();
        if (secondary == null) {
            dismissPresentation();
            return;
        }

        mainHandler.post(() -> {
            if (presentation == null || presentation.getDisplay() == null
                    || presentation.getDisplay().getDisplayId() != secondary.getDisplayId()) {
                dismissPresentation();
                presentation = new SecondaryDisplayPresentation(activity, secondary);
                presentation.show();
            }
            presentation.setMode(mode, background, overlayLabel);
        });
    }

    private void dismissPresentation() {
        mainHandler.post(() -> {
            if (presentation != null) {
                presentation.dismiss();
                presentation = null;
            }
        });
    }

    private Display findSecondaryDisplay() {
        if (displayManager == null) {
            return null;
        }
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        int defaultId = defaultDisplay != null ? defaultDisplay.getDisplayId() : Display.DEFAULT_DISPLAY;

        for (Display display : displayManager.getDisplays()) {
            if (display.getDisplayId() != defaultId && display.getState() == Display.STATE_ON) {
                return display;
            }
        }
        return null;
    }

    @Override
    public void onDisplayAdded(int displayId) {
        if (currentMode != SecondaryDisplayMode.OFF) {
            activity.runOnUiThread(() -> updateForScreen(activity.getCurrentScreen()));
        }
    }

    @Override
    public void onDisplayRemoved(int displayId) {
        dismissPresentation();
    }

    @Override
    public void onDisplayChanged(int displayId) {
    }
}
