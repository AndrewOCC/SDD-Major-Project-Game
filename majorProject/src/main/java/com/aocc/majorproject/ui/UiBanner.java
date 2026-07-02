package com.aocc.majorproject.ui;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;

/** Large centred prompt or heading text (e.g. "Game Over!", "Press anywhere to start"). */
public class UiBanner {

    private final float textSize;
    private final int color;

    public UiBanner(float textSize) {
        this(textSize, Color.WHITE);
    }

    public UiBanner(float textSize, int color) {
        this.textSize = textSize;
        this.color = color;
    }

    public void paint(Graphics g, Paint paint, String text, int centerX, int centerY) {
        float previousSize = paint.getTextSize();
        paint.setTextSize(textSize);
        UiText.drawCentered(g, paint, text, centerX, centerY, color);
        paint.setTextSize(previousSize);
    }
}
