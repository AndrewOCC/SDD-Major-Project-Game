package com.aocc.framework.implementation

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.Graphics.ImageFormat
import com.aocc.framework.Image
import com.aocc.framework.Viewport
import com.aocc.majorproject.AssetScale
import java.io.IOException
import java.io.InputStream

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

/**
 * Renders the game into an off-screen buffer sized to the letterboxed viewport,
 * then blits once to the surface. World coordinates stay 1280×720; the buffer
 * scale matches [Viewport.getScale] for sharp output without per-primitive
 * surface scaling cost.
 */
class AndroidGraphics(private val assets: AssetManager) : Graphics {

    private var canvas: Canvas? = null
    private val paint = Paint()
    private val srcRect = Rect()
    private val dstRect = Rect()
    private val filteredBitmapPaint =
        Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var frameBuffer: Bitmap? = null
        private set
    private var frameCanvas: Canvas? = null
    private var frameScale: Float = 1f

    fun ensureFramebuffer(viewport: Viewport) {
        val dest = viewport.getLetterboxDestRect()
        val width = dest.width()
        val height = dest.height()
        if (width <= 0 || height <= 0) {
            return
        }
        val current = frameBuffer
        if (current == null || current.width != width || current.height != height) {
            current?.recycle()
            val fresh = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            frameBuffer = fresh
            frameCanvas = Canvas(fresh)
        }
        frameScale = viewport.getScale()
    }

    /** Begin painting into the off-screen buffer for this frame. Returns false if not ready yet. */
    fun beginFrame(viewport: Viewport): Boolean {
        ensureFramebuffer(viewport)
        val frameCanvas = frameCanvas ?: return false
        canvas = frameCanvas
        frameCanvas.drawColor(Color.BLACK)
        frameCanvas.save()
        frameCanvas.scale(frameScale, frameScale)
        return true
    }

    fun endFrame() {
        canvas?.let {
            it.restore()
            canvas = null
        }
    }

    private fun requireCanvas(): Canvas {
        return canvas ?: throw IllegalStateException("beginFrame() must be called before drawing")
    }

    override fun newImage(fileName: String, format: ImageFormat): Image {
        return newImage(fileName, format, 1)
    }

    override fun newImage(fileName: String, format: ImageFormat, pixelScale: Int): Image {
        val has2x = assetExists(AssetScale.TWO_X_FOLDER + fileName)
        val path = AssetScale.resolvePath(fileName, pixelScale, has2x)
        val effectiveScale = AssetScale.effectivePixelScale(pixelScale, has2x)
        return decodeImage(path, format, effectiveScale)
    }

    private fun assetExists(path: String): Boolean {
        return try {
            assets.open(path).close()
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun decodeImage(fileName: String, format: ImageFormat, pixelScale: Int): Image {
        var resolvedFormat = format
        val config = when (format) {
            ImageFormat.RGB565 -> Bitmap.Config.RGB_565
            ImageFormat.ARGB4444 -> Bitmap.Config.ARGB_4444
            else -> Bitmap.Config.ARGB_8888
        }

        val options = BitmapFactory.Options()
        options.inPreferredConfig = config

        var `in`: InputStream? = null
        val bitmap: Bitmap
        try {
            `in` = assets.open(fileName)
            bitmap = BitmapFactory.decodeStream(`in`, null, options)
                ?: throw RuntimeException("Couldn't load bitmap from asset '$fileName'")
        } catch (e: IOException) {
            throw RuntimeException("Couldn't load bitmap from asset '$fileName'")
        } finally {
            try {
                `in`?.close()
            } catch (e: IOException) {
            }
        }

        resolvedFormat = when (bitmap.config) {
            Bitmap.Config.RGB_565 -> ImageFormat.RGB565
            Bitmap.Config.ARGB_4444 -> ImageFormat.ARGB4444
            else -> ImageFormat.ARGB8888
        }

        return AndroidImage(bitmap, resolvedFormat, pixelScale)
    }

    override fun clearScreen(color: Int) {
        val c = requireCanvas()
        paint.color = color
        paint.style = Paint.Style.FILL
        c.drawRect(0f, 0f, GameConstants.WORLD_WIDTH.toFloat(), GameConstants.WORLD_HEIGHT.toFloat(), paint)
    }

    override fun drawLine(x: Int, y: Int, x2: Int, y2: Int, color: Int) {
        paint.color = color
        requireCanvas().drawLine(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), paint)
    }

    override fun drawCircle(x: Float, y: Float, radius: Float, color: Int) {
        paint.color = color
        requireCanvas().drawCircle(x, y, radius, paint)
    }

    override fun drawArc(
        oval: RectF,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean,
        color: Int
    ) {
        paint.isAntiAlias = true
        paint.color = color
        requireCanvas().drawArc(oval, startAngle, sweepAngle, useCenter, paint)
    }

    override fun drawButton(x: Int, y: Int, height: Int, color: Int, text: String) {
        requireCanvas().drawText(text, x.toFloat(), y.toFloat(), paint)
    }

    override fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int) {
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        requireCanvas().drawRect(
            x.toFloat(),
            y.toFloat(),
            (x + width - 1).toFloat(),
            (y + height - 1).toFloat(),
            paint
        )
    }

    override fun drawRectOutline(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Int,
        strokeWidth: Float
    ) {
        strokePaint.color = color
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = strokeWidth
        val half = strokeWidth / 2f
        requireCanvas().drawRect(
            x + half,
            y + half,
            x + width - half,
            y + height - half,
            strokePaint
        )
    }

    override fun drawCircleOutline(
        centerX: Float,
        centerY: Float,
        radius: Float,
        color: Int,
        strokeWidth: Float
    ) {
        strokePaint.color = color
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = strokeWidth
        requireCanvas().drawCircle(centerX, centerY, radius - strokeWidth / 2f, strokePaint)
    }

    override fun drawARGB(a: Int, r: Int, g: Int, b: Int) {
        paint.style = Paint.Style.FILL
        paint.setARGB(a, r, g, b)
        requireCanvas().drawRect(
            0f,
            0f,
            GameConstants.WORLD_WIDTH.toFloat(),
            GameConstants.WORLD_HEIGHT.toFloat(),
            paint
        )
    }

    override fun drawString(text: String, x: Int, y: Int, color: Int, textPaint: Paint) {
        textPaint.color = color
        requireCanvas().drawText(text, x.toFloat(), y.toFloat(), textPaint)
    }

    override fun drawImage(
        image: Image,
        x: Int,
        y: Int,
        srcX: Int,
        srcY: Int,
        srcWidth: Int,
        srcHeight: Int
    ) {
        val androidImage = image as AndroidImage
        val pixelScale = androidImage.pixelScale

        srcRect.left = srcX
        srcRect.top = srcY
        srcRect.right = srcX + srcWidth
        srcRect.bottom = srcY + srcHeight

        if (pixelScale == 1 && srcX == 0 && srcY == 0
            && srcWidth == androidImage.bitmap.width
            && srcHeight == androidImage.bitmap.height
        ) {
            requireCanvas().drawBitmap(androidImage.bitmap, x.toFloat(), y.toFloat(), null)
            return
        }

        dstRect.left = x
        dstRect.top = y
        dstRect.right = x + srcWidth / pixelScale
        dstRect.bottom = y + srcHeight / pixelScale

        requireCanvas().drawBitmap(androidImage.bitmap, srcRect, dstRect, filteredBitmapPaint)
    }

    override fun drawImage(image: Image, x: Int, y: Int) {
        val androidImage = image as AndroidImage
        if (androidImage.pixelScale == 1) {
            requireCanvas().drawBitmap(androidImage.bitmap, x.toFloat(), y.toFloat(), null)
            return
        }
        dstRect.left = x
        dstRect.top = y
        dstRect.right = x + androidImage.width
        dstRect.bottom = y + androidImage.height
        requireCanvas().drawBitmap(androidImage.bitmap, null, dstRect, filteredBitmapPaint)
    }

    fun drawScaledImage(
        image: Image,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        srcX: Int,
        srcY: Int,
        srcWidth: Int,
        srcHeight: Int
    ) {
        val androidImage = image as AndroidImage

        srcRect.left = srcX
        srcRect.top = srcY
        srcRect.right = srcX + srcWidth
        srcRect.bottom = srcY + srcHeight

        dstRect.left = x
        dstRect.top = y
        dstRect.right = x + width
        dstRect.bottom = y + height

        requireCanvas().drawBitmap(androidImage.bitmap, srcRect, dstRect, filteredBitmapPaint)
    }

    override val width: Int
        get() = GameConstants.WORLD_WIDTH

    override val height: Int
        get() = GameConstants.WORLD_HEIGHT
}
