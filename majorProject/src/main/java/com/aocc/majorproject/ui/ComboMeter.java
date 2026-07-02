package com.aocc.majorproject.ui;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.GameConstants;
import com.aocc.framework.Graphics;

/** Combo multiplier strip shown to the left of the score bar. */
public class ComboMeter {

    private static final int BAR_HEIGHT = ScoreBar.BAR_HEIGHT;
    private static final int BAR_WIDTH = 120;
    private static final int GAP = 8;
    private static final int BAR_X = ScoreBar.BAR_X - BAR_WIDTH - GAP;

    private final UiBounds bounds = new UiBounds(BAR_X, 0, BAR_WIDTH, BAR_HEIGHT);
    private final int backgroundColor = Color.argb(100, 255, 255, 255);
    private final int textColor = Color.WHITE;
    private final float textSize = 40f;

    public UiBounds getBounds() {
        return bounds;
    }

    public void paint(Graphics g, Paint paint, int combo) {
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, backgroundColor);
        float previousSize = paint.getTextSize();
        paint.setTextSize(textSize);
        UiText.drawInBounds(g, paint, "x" + combo, bounds, UiText.HAlign.CENTER, textColor);
        paint.setTextSize(previousSize);
    }
}
