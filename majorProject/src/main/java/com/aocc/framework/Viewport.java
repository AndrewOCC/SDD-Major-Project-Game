package com.aocc.framework;

import android.graphics.Rect;

/**
 * Maps between virtual world coordinates and the letterboxed on-screen area.
 */
public class Viewport {

    private float scale = 1f;
    private float offsetX;
    private float offsetY;
    private int viewWidth;
    private int viewHeight;

    public void update(int viewWidth, int viewHeight) {
        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;

        float scaleX = (float) viewWidth / GameConstants.WORLD_WIDTH;
        float scaleY = (float) viewHeight / GameConstants.WORLD_HEIGHT;
        scale = Math.min(scaleX, scaleY);

        float contentWidth = GameConstants.WORLD_WIDTH * scale;
        float contentHeight = GameConstants.WORLD_HEIGHT * scale;
        offsetX = (viewWidth - contentWidth) * 0.5f;
        offsetY = (viewHeight - contentHeight) * 0.5f;
    }

    public int screenToWorldX(float screenX) {
        return Math.round((screenX - offsetX) / scale);
    }

    public int screenToWorldY(float screenY) {
        return Math.round((screenY - offsetY) / scale);
    }

    public Rect getLetterboxDestRect() {
        Rect rect = new Rect();
        rect.left = Math.round(offsetX);
        rect.top = Math.round(offsetY);
        rect.right = Math.round(offsetX + GameConstants.WORLD_WIDTH * scale);
        rect.bottom = Math.round(offsetY + GameConstants.WORLD_HEIGHT * scale);
        return rect;
    }

    public float getScale() {
        return scale;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }
}
