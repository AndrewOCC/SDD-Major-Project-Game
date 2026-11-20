package com.aocc.framework

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

import android.graphics.Paint
import android.graphics.RectF

interface Graphics {
    enum class ImageFormat {
        ARGB8888, ARGB4444, RGB565
    }

    fun newImage(fileName: String, format: ImageFormat): Image

    /**
     * Loads a bitmap, preferring a 2× asset in `2x/<fileName>` when
     * `pixelScale` is 2 and that file exists. Default implementation
     * ignores `pixelScale`.
     */
    fun newImage(fileName: String, format: ImageFormat, pixelScale: Int): Image {
        return newImage(fileName, format)
    }

    fun clearScreen(color: Int)

    fun drawLine(x: Int, y: Int, x2: Int, y2: Int, color: Int)

    fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int)

    /** Stroked rectangle outline (transparent interior) for focus rings. */
    fun drawRectOutline(x: Int, y: Int, width: Int, height: Int, color: Int, strokeWidth: Float)

    /** Stroked circle outline (transparent interior) for focus rings. */
    fun drawCircleOutline(centerX: Float, centerY: Float, radius: Float, color: Int, strokeWidth: Float)

    fun drawImage(image: Image, x: Int, y: Int, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)

    fun drawImage(image: Image, x: Int, y: Int)

    /** Line with a round cap of the given stroke [width], in world units. */
    fun drawStroke(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, width: Float)

    /** Draws [image] stretched (filtered) to fill the rectangle at ([x], [y]) sized [width] x [height]. */
    fun drawImageStretched(image: Image, x: Int, y: Int, width: Int, height: Int)

    /** Draws [image] centred on ([centerX], [centerY]), scaled, rotated and faded (alpha 0..255). */
    fun drawImageTransformed(
        image: Image,
        centerX: Float,
        centerY: Float,
        scale: Float,
        rotationDegrees: Float,
        alpha: Int,
    )

    /**
     * Draws a sub-rectangle of [image] (in the image's world units, so sprite sheets work at any
     * asset pixel scale) centred on ([centerX], [centerY]) at [width] x [height] world units,
     * colour-multiplied by [tint] (white leaves it unchanged).
     */
    fun drawImageRegion(
        image: Image,
        srcX: Int,
        srcY: Int,
        srcWidth: Int,
        srcHeight: Int,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        tint: Int = android.graphics.Color.WHITE,
    )

    fun drawString(text: String, x: Int, y: Int, color: Int, paint: Paint)

    val width: Int

    val height: Int

    fun drawARGB(i: Int, j: Int, k: Int, l: Int)

    // The following two methods were created as part of the Major Project:
    fun drawCircle(x: Float, y: Float, radius: Float, color: Int)

    fun drawArc(oval: RectF, startAngle: Float, sweepAngle: Float, useCenter: Boolean, color: Int)

    //Intelligently draws a button of appropriate width given an input string/location
    fun drawButton(x: Int, y: Int, height: Int, color: Int, text: String)
}
