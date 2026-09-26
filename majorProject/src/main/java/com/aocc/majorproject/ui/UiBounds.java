package com.aocc.majorproject.ui;

import com.aocc.framework.Input;

/** Axis-aligned rectangle in world coordinates (1280×720). */
public final class UiBounds {

    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public UiBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int centerX() {
        return x + width / 2;
    }

    public int centerY() {
        return y + height / 2;
    }

    public boolean contains(float touchX, float touchY) {
        return touchX > x && touchX < x + width - 1
                && touchY > y && touchY < y + height - 1;
    }

    public boolean contains(Input.TouchEvent event) {
        return contains(event.x, event.y);
    }

    /** Returns a smaller rectangle inset on all sides (for visual focus rings). */
    public UiBounds inset(int horizontal, int vertical) {
        return new UiBounds(x + horizontal, y + vertical,
                width - horizontal * 2, height - vertical * 2);
    }
}
