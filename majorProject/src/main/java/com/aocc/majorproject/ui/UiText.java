package com.aocc.majorproject.ui;

import android.graphics.Paint;

import com.aocc.framework.Graphics;

/** Text rendering with correct horizontal and vertical alignment within bounds. */
public final class UiText {

    public enum HAlign {
        LEFT(Paint.Align.LEFT),
        CENTER(Paint.Align.CENTER),
        RIGHT(Paint.Align.RIGHT);

        private final Paint.Align paintAlign;

        HAlign(Paint.Align paintAlign) {
            this.paintAlign = paintAlign;
        }
    }

    private UiText() {
    }

    public static void drawInBounds(Graphics g, Paint paint, String text, UiBounds bounds,
            HAlign hAlign, int color) {
        paint.setTextAlign(hAlign.paintAlign);
        int anchorX;
        switch (hAlign) {
            case LEFT:
                anchorX = bounds.x;
                break;
            case RIGHT:
                anchorX = bounds.x + bounds.width;
                break;
            default:
                anchorX = bounds.centerX();
                break;
        }
        drawAtBaseline(g, paint, text, anchorX, bounds.centerY(), color);
    }

    public static void drawCentered(Graphics g, Paint paint, String text, int centerX, int centerY,
            int color) {
        paint.setTextAlign(Paint.Align.CENTER);
        drawAtBaseline(g, paint, text, centerX, centerY, color);
    }

    public static void drawLeftOfCenter(Graphics g, Paint paint, String text, int leftX, int centerY,
            int color) {
        paint.setTextAlign(Paint.Align.LEFT);
        drawAtBaseline(g, paint, text, leftX, centerY, color);
    }

    private static void drawAtBaseline(Graphics g, Paint paint, String text, int anchorX, int centerY,
            int color) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        float baseline = centerY - (fm.ascent + fm.descent) / 2f;
        g.drawString(text, anchorX, Math.round(baseline), color, paint);
    }
}
