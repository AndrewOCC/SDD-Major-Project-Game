package com.aocc.majorproject.ui;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;

/** Filled panel with an optional centred title above the panel body. */
public class UiPanel {

    private final UiBounds bounds;
    private final int backgroundColor;
    private final float titleTextSize;
    private final int titleColor = Color.WHITE;
    private final int titleGap = 30;

    public UiPanel(int x, int y, int width, int height, int backgroundColor) {
        this.bounds = new UiBounds(x, y, width, height);
        this.backgroundColor = backgroundColor;
        this.titleTextSize = 30f;
    }

    public UiBounds getBounds() {
        return bounds;
    }

    public void paintBackground(Graphics g) {
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, backgroundColor);
    }

    public void paintTitle(Graphics g, Paint paint, String title) {
        float previousSize = paint.getTextSize();
        paint.setTextSize(titleTextSize);
        int titleCenterY = bounds.y - titleGap;
        UiText.drawCentered(g, paint, title, bounds.centerX(), titleCenterY, titleColor);
        paint.setTextSize(previousSize);
    }
}
