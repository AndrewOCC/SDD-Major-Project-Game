package com.aocc.majorproject.ui;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;
import com.aocc.framework.Input;
import com.aocc.majorproject.Assets;

/** Rectangular label button with centered text and optional pressed state. */
public class UiButton {

    public static final int MENU_WIDTH = 200;
    public static final int MENU_HEIGHT = 70;
    public static final float MENU_TEXT_SIZE = 50f;

    private static final Paint LABEL_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final UiBounds bounds;
    private final String label;
    private final float textSize;
    private int backgroundColor = Color.DKGRAY;
    private int pressedBackgroundColor = Color.rgb(195, 195, 195);
    private int textColor = Color.WHITE;
    private boolean pressed;

    public UiButton(int x, int y, int width, int height, String label) {
        this(x, y, width, height, label, MENU_TEXT_SIZE);
    }

    public UiButton(int x, int y, int width, int height, String label, float textSize) {
        this.bounds = new UiBounds(x, y, width, height);
        this.label = label;
        this.textSize = textSize;
    }

    public static UiButton menuAt(int x, int y) {
        return new UiButton(x, y, MENU_WIDTH, MENU_HEIGHT, "Menu");
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public void paint(Graphics g) {
        int bg = pressed ? pressedBackgroundColor : backgroundColor;
        g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, bg);

        if (Assets.plain != null) {
            LABEL_PAINT.setTypeface(Assets.plain);
        }
        LABEL_PAINT.setTextSize(textSize);
        LABEL_PAINT.setColor(textColor);
        UiText.drawInBounds(g, LABEL_PAINT, label, bounds, UiText.HAlign.CENTER, textColor);
    }

    /** @deprecated Use {@link #paint(Graphics)} — label rendering no longer shares caller paint. */
    public void paint(Graphics g, Paint paint) {
        paint(g);
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
