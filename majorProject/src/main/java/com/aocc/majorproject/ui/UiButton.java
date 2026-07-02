package com.aocc.majorproject.ui;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;
import com.aocc.framework.Input;

/** Rectangular label button with centered text and optional pressed state. */
public class UiButton {

    private final UiBounds bounds;
    private final String label;
    private int backgroundColor = Color.DKGRAY;
    private int pressedBackgroundColor = Color.rgb(195, 195, 195);
    private int textColor = Color.WHITE;
    private boolean pressed;

    public UiButton(int x, int y, int width, int height, String label) {
        this.bounds = new UiBounds(x, y, width, height);
        this.label = label;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public void paint(Graphics g, Paint paint) {
        int bg = pressed ? pressedBackgroundColor : backgroundColor;
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, bg);
        UiText.drawInBounds(g, paint, label, bounds, UiText.HAlign.CENTER, textColor);
    }

    public boolean touchInBounds(Input.TouchEvent event) {
        return bounds.contains(event);
    }

    public UiBounds getBounds() {
        return bounds;
    }

    public String getLabel() {
        return label;
    }
}
