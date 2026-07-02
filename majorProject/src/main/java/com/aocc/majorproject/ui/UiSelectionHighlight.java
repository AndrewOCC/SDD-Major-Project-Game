package com.aocc.majorproject.ui;

import android.graphics.Color;

import com.aocc.framework.Graphics;

/** Gamepad / keyboard focus ring — outline only, does not cover button art. */
public final class UiSelectionHighlight {

    public static final int COLOR = Color.rgb(255, 220, 80);
    public static final float STROKE_WIDTH = 3f;

    private UiSelectionHighlight() {
    }

    public static void paintRect(Graphics g, UiBounds bounds) {
        g.drawRectOutline(bounds.x, bounds.y, bounds.width, bounds.height, COLOR, STROKE_WIDTH);
    }

    public static void paintCircle(Graphics g, int centerX, int centerY, int radius) {
        g.drawCircleOutline(centerX, centerY, radius, COLOR, STROKE_WIDTH);
    }
}
