package com.aocc.framework.implementation;

import android.graphics.Bitmap;

import com.aocc.framework.Image;
import com.aocc.framework.Graphics.ImageFormat;

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

public class AndroidImage implements Image {
    private final Bitmap bitmap;
    private final ImageFormat format;
    /** 1 for standard assets; 2 when loaded from the 2× folder (logical size = pixels ÷ 2). */
    private final int pixelScale;

    public AndroidImage(Bitmap bitmap, ImageFormat format) {
        this(bitmap, format, 1);
    }

    public AndroidImage(Bitmap bitmap, ImageFormat format, int pixelScale) {
        this.bitmap = bitmap;
        this.format = format;
        this.pixelScale = Math.max(1, pixelScale);
    }

    @Override
    public int getWidth() {
        return bitmap.getWidth() / pixelScale;
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight() / pixelScale;
    }

    public int getPixelScale() {
        return pixelScale;
    }

    @Override
    public ImageFormat getFormat() {
        return format;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public void dispose() {
        bitmap.recycle();
    }
}
