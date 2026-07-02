package com.aocc.majorproject;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;

public final class VersionOverlay {

    private static final int VERSION_X = 1265;
    private static final int VERSION_Y = 710;
    private static final float VERSION_TEXT_SIZE = 18f;

    private static final Paint VERSION_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);

    private VersionOverlay() {
    }

    public static void paint(Graphics g) {
        if (Assets.plain != null) {
            VERSION_PAINT.setTypeface(Assets.plain);
        }
        VERSION_PAINT.setTextAlign(Paint.Align.RIGHT);
        VERSION_PAINT.setTextSize(VERSION_TEXT_SIZE);
        VERSION_PAINT.setColor(Color.argb(170, 255, 255, 255));
        g.drawString("v" + BuildConfig.VERSION_NAME, VERSION_X, VERSION_Y,
                VERSION_PAINT.getColor(), VERSION_PAINT);
    }

    /** @deprecated Use {@link #paint(Graphics)} — version label uses its own paint. */
    public static void paint(Graphics g, Paint paint) {
        paint(g);
    }
}
