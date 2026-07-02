package com.aocc.majorproject.display;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.WindowManager;

import com.aocc.majorproject.Assets;
import com.aocc.majorproject.CrashReporter;
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
    private PendingShow pendingShow;

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
        dismissPresentationSync();
    }

    public void refresh() {
        if (GamePreferences.secondScreenEnabled) {
            updateForScreen(activity.getCurrentScreen());
        }
    }

    public boolean isSecondaryDisplayAvailable() {
        return SecondaryDisplayFinder.find(activity, displayManager) != null;
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
        if (state == null) {
            show(SecondaryDisplayMode.OFF, null, null);
            return;
        }
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
            pendingShow = null;
            dismissPresentationAsync();
            return;
        }

        Display secondary = SecondaryDisplayFinder.find(activity, displayManager);
        if (secondary == null) {
            pendingShow = new PendingShow(mode, background, overlayLabel);
            dismissPresentationAsync();
            return;
        }

        pendingShow = new PendingShow(mode, background, overlayLabel);
        mainHandler.post(() -> presentOnDisplay(secondary, pendingShow));
    }

    private void presentOnDisplay(Display secondary, PendingShow request) {
        if (request == null || !GamePreferences.secondScreenEnabled) {
            return;
        }

        try {
            if (presentation == null || presentation.getDisplay() == null
                    || presentation.getDisplay().getDisplayId() != secondary.getDisplayId()) {
                dismissPresentationSync();
                presentation = new SecondaryDisplayPresentation(activity, secondary);
                presentation.show();
            }
            presentation.setMode(request.mode, request.background, request.overlayLabel);
            pendingShow = null;
        } catch (WindowManager.InvalidDisplayException e) {
            CrashReporter.log(activity, "Secondary display rejected Presentation.show()", e);
            dismissPresentationSync();
        } catch (RuntimeException e) {
            CrashReporter.log(activity, "Failed to show secondary display", e);
            dismissPresentationSync();
        }
    }

    private void dismissPresentationAsync() {
        mainHandler.post(this::dismissPresentationSync);
    }

    private void dismissPresentationSync() {
        if (presentation != null) {
            try {
                presentation.dismiss();
            } catch (RuntimeException e) {
                CrashReporter.log(activity, "Failed to dismiss secondary display", e);
            }
            presentation = null;
        }
    }

    @Override
    public void onDisplayAdded(int displayId) {
        if (GamePreferences.secondScreenEnabled) {
            activity.runOnUiThread(() -> {
                if (pendingShow != null) {
                    show(pendingShow.mode, pendingShow.background, pendingShow.overlayLabel);
                } else {
                    updateForScreen(activity.getCurrentScreen());
                }
            });
        }
    }

    @Override
    public void onDisplayRemoved(int displayId) {
        if (presentation != null && presentation.getDisplay() != null
                && presentation.getDisplay().getDisplayId() == displayId) {
            dismissPresentationAsync();
        }
    }

    @Override
    public void onDisplayChanged(int displayId) {
        if (!GamePreferences.secondScreenEnabled) {
            return;
        }
        if (presentation == null || presentation.getDisplay() == null
                || presentation.getDisplay().getDisplayId() != displayId) {
            activity.runOnUiThread(() -> updateForScreen(activity.getCurrentScreen()));
        }
    }

    private static final class PendingShow {
        final SecondaryDisplayMode mode;
        final com.aocc.framework.Image background;
        final String overlayLabel;

        PendingShow(SecondaryDisplayMode mode, com.aocc.framework.Image background,
                String overlayLabel) {
            this.mode = mode;
            this.background = background;
            this.overlayLabel = overlayLabel;
        }
    }
}
