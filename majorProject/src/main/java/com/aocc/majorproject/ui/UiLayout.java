package com.aocc.majorproject.ui;

import com.aocc.framework.GameConstants;

/** Helpers for placing UI within the 1280×720 world rectangle. */
public final class UiLayout {

    private UiLayout() {
    }

    public static int centerX(int width) {
        return (GameConstants.WORLD_WIDTH - width) / 2;
    }

    public static int centerY(int height) {
        return (GameConstants.WORLD_HEIGHT - height) / 2;
    }

    public static int alignRight(int width, int margin) {
        return GameConstants.WORLD_WIDTH - width - margin;
    }
}
