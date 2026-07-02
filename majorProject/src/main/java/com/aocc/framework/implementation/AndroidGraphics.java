package com.aocc.framework.implementation;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;

import com.aocc.framework.GameConstants;
import com.aocc.framework.Graphics;
import com.aocc.framework.Image;
import com.aocc.framework.Viewport;

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

/**
 * Draws in virtual world coordinates ({@link GameConstants#WORLD_WIDTH}×
 * {@link GameConstants#WORLD_HEIGHT}). Call {@link #beginFrame(Canvas, Viewport)}
 * before painting each frame; the viewport transform maps world space to native
 * screen pixels.
 */
public class AndroidGraphics implements Graphics {
    private final AssetManager assets;
    private Canvas canvas;
    private final Paint paint;
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();
    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

    public AndroidGraphics(AssetManager assets) {
        this.assets = assets;
        this.paint = new Paint();
    }

    public void beginFrame(Canvas target, Viewport viewport) {
        this.canvas = target;
        canvas.save();
        canvas.translate(viewport.getOffsetX(), viewport.getOffsetY());
        canvas.scale(viewport.getScale(), viewport.getScale());
    }

    public void endFrame() {
        if (canvas != null) {
            canvas.restore();
            canvas = null;
        }
    }

    private Canvas requireCanvas() {
        if (canvas == null) {
            throw new IllegalStateException("beginFrame() must be called before drawing");
        }
        return canvas;
    }

    @Override
    public Image newImage(String fileName, ImageFormat format) {
        Config config = null;
        if (format == ImageFormat.RGB565)
            config = Config.RGB_565;
        else if (format == ImageFormat.ARGB4444)
            config = Config.ARGB_4444;
        else
            config = Config.ARGB_8888;

        Options options = new Options();
        options.inPreferredConfig = config;

        InputStream in = null;
        Bitmap bitmap = null;
        try {
            in = assets.open(fileName);
            bitmap = BitmapFactory.decodeStream(in, null, options);
            if (bitmap == null)
                throw new RuntimeException("Couldn't load bitmap from asset '"
                        + fileName + "'");
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load bitmap from asset '"
                    + fileName + "'");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        if (bitmap.getConfig() == Config.RGB_565)
            format = ImageFormat.RGB565;
        else if (bitmap.getConfig() == Config.ARGB_4444)
            format = ImageFormat.ARGB4444;
        else
            format = ImageFormat.ARGB8888;

        return new AndroidImage(bitmap, format);
    }

    @Override
    public void clearScreen(int color) {
        Canvas c = requireCanvas();
        paint.setColor(color);
        paint.setStyle(Style.FILL);
        c.drawRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, paint);
    }

    @Override
    public void drawLine(int x, int y, int x2, int y2, int color) {
        paint.setColor(color);
        requireCanvas().drawLine(x, y, x2, y2, paint);
    }

    @Override
    public void drawCircle(float x, float y, float radius, int color) {
        paint.setColor(color);
        requireCanvas().drawCircle(x, y, radius, paint);
    }

    @Override
    public void drawArc(RectF oval, float startAngle, float sweepAngle,
            boolean useCenter, int color) {
        paint.setAntiAlias(true);
        paint.setColor(color);
        requireCanvas().drawArc(oval, startAngle, sweepAngle, useCenter, paint);
    }

    @Override
    public void drawButton(int x, int y, int height, int color, String text) {
        requireCanvas().drawText(text, x, y, paint);
    }

    @Override
    public void drawRect(int x, int y, int width, int height, int color) {
        paint.setColor(color);
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        requireCanvas().drawRect(x, y, x + width - 1, y + height - 1, paint);
    }

    @Override
    public void drawARGB(int a, int r, int g, int b) {
        paint.setStyle(Style.FILL);
        paint.setARGB(a, r, g, b);
        requireCanvas().drawRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, paint);
    }

    @Override
    public void drawString(String text, int x, int y, int color, Paint textPaint) {
        textPaint.setColor(color);
        requireCanvas().drawText(text, x, y, textPaint);
    }

    public void drawImage(Image Image, int x, int y, int srcX, int srcY,
            int srcWidth, int srcHeight) {
        srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + srcWidth;
        srcRect.bottom = srcY + srcHeight;

        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x + srcWidth;
        dstRect.bottom = y + srcHeight;

        requireCanvas().drawBitmap(((AndroidImage) Image).bitmap, srcRect, dstRect,
                bitmapPaint);
    }

    @Override
    public void drawImage(Image Image, int x, int y) {
        requireCanvas().drawBitmap(((AndroidImage) Image).bitmap, x, y, bitmapPaint);
    }

    public void drawScaledImage(Image Image, int x, int y, int width, int height, int srcX, int srcY, int srcWidth, int srcHeight) {
        srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + srcWidth;
        srcRect.bottom = srcY + srcHeight;

        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x + width;
        dstRect.bottom = y + height;

        requireCanvas().drawBitmap(((AndroidImage) Image).bitmap, srcRect, dstRect, bitmapPaint);
    }

    @Override
    public int getWidth() {
        return GameConstants.WORLD_WIDTH;
    }

    @Override
    public int getHeight() {
        return GameConstants.WORLD_HEIGHT;
    }
}
