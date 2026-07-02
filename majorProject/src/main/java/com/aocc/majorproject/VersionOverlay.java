package com.aocc.majorproject;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;

public final class VersionOverlay {

    private static final int VERSION_X = 1265;
    private static final int VERSION_Y = 710;

    private VersionOverlay() {
    }

    public static void paint(Graphics g, Paint paint) {
        if (Assets.plain != null) {
            paint.setTypeface(Assets.plain);
        }
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(18);
        g.drawString("v" + BuildConfig.VERSION_NAME, VERSION_X, VERSION_Y,
                Color.argb(170, 255, 255, 255), paint);
    }
}
