package com.aocc.majorproject.ui;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.GameConstants;
import com.aocc.framework.Graphics;

/** Top-centre score strip with semi-transparent background and centred label. */
public class ScoreBar {

    private static final int BAR_WIDTH = 320;
    private static final int BAR_HEIGHT = 50;
    private static final int BAR_X = (GameConstants.WORLD_WIDTH - BAR_WIDTH) / 2;

    private final UiBounds bounds = new UiBounds(BAR_X, 0, BAR_WIDTH, BAR_HEIGHT);
    private final int backgroundColor = Color.argb(100, 255, 255, 255);
    private final int textColor = Color.WHITE;
    private final float textSize = 40f;

    public void paint(Graphics g, Paint paint, int score) {
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, backgroundColor);
        float previousSize = paint.getTextSize();
        paint.setTextSize(textSize);
        UiText.drawInBounds(g, paint, "SCORE: " + score, bounds, UiText.HAlign.CENTER, textColor);
        paint.setTextSize(previousSize);
    }
}
