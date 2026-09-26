package com.aocc.framework.implementation;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;

import com.aocc.framework.GameConstants;
import com.aocc.framework.Graphics;
import com.aocc.framework.Image;
import com.aocc.framework.Viewport;
import com.aocc.majorproject.AssetScale;

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

/**
 * Renders the game into an off-screen buffer sized to the letterboxed viewport,
 * then blits once to the surface. World coordinates stay 1280×720; the buffer
 * scale matches {@link Viewport#getScale()} for sharp output without per-primitive
 * surface scaling cost.
 */
public class AndroidGraphics implements Graphics {
    private final AssetManager assets;
    private Canvas canvas;
    private final Paint paint;
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();
    private final Paint filteredBitmapPaint =
            new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap frameBuffer;
    private Canvas frameCanvas;
    private float frameScale = 1f;

    public AndroidGraphics(AssetManager assets) {
        this.assets = assets;
        this.paint = new Paint();
    }

    public void ensureFramebuffer(Viewport viewport) {
        Rect dest = viewport.getLetterboxDestRect();
        int width = dest.width();
        int height = dest.height();
        if (width <= 0 || height <= 0) {
            return;
        }
        if (frameBuffer == null
                || frameBuffer.getWidth() != width
                || frameBuffer.getHeight() != height) {
            if (frameBuffer != null) {
                frameBuffer.recycle();
            }
            frameBuffer = Bitmap.createBitmap(width, height, Config.RGB_565);
            frameCanvas = new Canvas(frameBuffer);
        }
        frameScale = viewport.getScale();
    }

    public Bitmap getFrameBuffer() {
        return frameBuffer;
    }

    /** Begin painting into the off-screen buffer for this frame. */
    public void beginFrame(Viewport viewport) {
        ensureFramebuffer(viewport);
        if (frameCanvas == null) {
            throw new IllegalStateException("Framebuffer not ready");
        }
        canvas = frameCanvas;
        canvas.drawColor(Color.BLACK);
        canvas.save();
        canvas.scale(frameScale, frameScale);
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
        return newImage(fileName, format, 1);
    }

    @Override
    public Image newImage(String fileName, ImageFormat format, int pixelScale) {
        boolean has2x = assetExists(AssetScale.TWO_X_FOLDER + fileName);
        String path = AssetScale.resolvePath(fileName, pixelScale, has2x);
        int effectiveScale = AssetScale.effectivePixelScale(pixelScale, has2x);
        return decodeImage(path, format, effectiveScale);
    }

    private boolean assetExists(String path) {
        try {
            assets.open(path).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Image decodeImage(String fileName, ImageFormat format, int pixelScale) {
        Config config;
        if (format == ImageFormat.RGB565)
            config = Config.RGB_565;
        else if (format == ImageFormat.ARGB4444)
            config = Config.ARGB_4444;
        else
            config = Config.ARGB_8888;

        Options options = new Options();
        options.inPreferredConfig = config;

        InputStream in = null;
        Bitmap bitmap;
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

        return new AndroidImage(bitmap, format, pixelScale);
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
    public void drawRectOutline(int x, int y, int width, int height, int color,
            float strokeWidth) {
        strokePaint.setColor(color);
        strokePaint.setStyle(Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidth);
        float half = strokeWidth / 2f;
        requireCanvas().drawRect(
                x + half, y + half,
                x + width - half, y + height - half,
                strokePaint);
    }

    @Override
    public void drawCircleOutline(float centerX, float centerY, float radius, int color,
            float strokeWidth) {
        strokePaint.setColor(color);
        strokePaint.setStyle(Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidth);
        requireCanvas().drawCircle(centerX, centerY, radius - strokeWidth / 2f, strokePaint);
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

    public void drawImage(Image image, int x, int y, int srcX, int srcY,
            int srcWidth, int srcHeight) {
        AndroidImage androidImage = (AndroidImage) image;
        int pixelScale = androidImage.getPixelScale();

        srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + srcWidth;
        srcRect.bottom = srcY + srcHeight;

        if (pixelScale == 1 && srcX == 0 && srcY == 0
                && srcWidth == androidImage.getBitmap().getWidth()
                && srcHeight == androidImage.getBitmap().getHeight()) {
            requireCanvas().drawBitmap(androidImage.getBitmap(), x, y, null);
            return;
        }

        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x + srcWidth / pixelScale;
        dstRect.bottom = y + srcHeight / pixelScale;

        requireCanvas().drawBitmap(androidImage.getBitmap(), srcRect, dstRect,
                filteredBitmapPaint);
    }

    @Override
    public void drawImage(Image image, int x, int y) {
        AndroidImage androidImage = (AndroidImage) image;
        if (androidImage.getPixelScale() == 1) {
            requireCanvas().drawBitmap(androidImage.getBitmap(), x, y, null);
            return;
        }
        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x + androidImage.getWidth();
        dstRect.bottom = y + androidImage.getHeight();
        requireCanvas().drawBitmap(androidImage.getBitmap(), null, dstRect, filteredBitmapPaint);
    }

    public void drawScaledImage(Image image, int x, int y, int width, int height, int srcX, int srcY, int srcWidth, int srcHeight) {
        AndroidImage androidImage = (AndroidImage) image;

        srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + srcWidth;
        srcRect.bottom = srcY + srcHeight;

        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x + width;
        dstRect.bottom = y + height;

        requireCanvas().drawBitmap(androidImage.getBitmap(), srcRect, dstRect, filteredBitmapPaint);
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
