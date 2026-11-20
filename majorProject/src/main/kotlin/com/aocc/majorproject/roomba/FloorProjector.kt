package com.aocc.majorproject.roomba

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Typeface
import com.aocc.framework.Graphics
import com.aocc.framework.implementation.AndroidGraphics
import java.util.Random

/**
 * An old overhead projector throwing a "slide" of warm light onto the living-room floor.
 *
 * The slide is a bitmap of light: a bright, softly vignetted field with the text cut out of it,
 * like black marker on an acetate. It is added onto the floor through a keystoned quad. Things
 * standing on the floor ([Occluder]s: the Roomba, the mites, the dock) intercept the light. Their
 * tops show the slide displaced toward the projector by their height, and they cast a shadow
 * on the floor's copy of the slide behind them.
 *
 * Projector position is in world units (1 unit = 1 cm), [projectorHeight] above the floor.
 */
class FloorProjector(
    private val projectorX: Float,
    private val projectorY: Float,
    private val projectorHeight: Float,
    slideWidth: Int,
    slideHeight: Int,
) {

    class Occluder {
        var x = 0f
        var y = 0f
        var radius = 0f
        var height = 0f
    }

    /** Slide bitmap at 2x its world size for crisp edges after the keystone. */
    private val bitmap = Bitmap.createBitmap(slideWidth * 2, slideHeight * 2, Bitmap.Config.ARGB_8888)
    val slideCanvas = Canvas(bitmap)
    val slideWidthPx: Int get() = bitmap.width
    val slideHeightPx: Int get() = bitmap.height

    private val slideToFloor = Matrix()
    private val occluders = ArrayList<Occluder>()
    private var occluderCount = 0
    private val path = Path()
    private val lightPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    }
    private val fieldPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }
    private val fieldRect = RectF()
    private val specks: List<FloatArray>

    init {
        // A few bits of fluff and a hair stuck to the acetate.
        val random = Random(7L)
        specks = List(6) {
            floatArrayOf(
                random.nextFloat() * bitmap.width, random.nextFloat() * bitmap.height,
                (random.nextFloat() - 0.5f) * 40f, (random.nextFloat() - 0.5f) * 30f,
            )
        }
    }

    /** Where the slide lands on the floor: four world-space corners, clockwise from top-left. */
    fun setQuad(tlX: Float, tlY: Float, trX: Float, trY: Float, brX: Float, brY: Float, blX: Float, blY: Float) {
        val w = bitmap.width.toFloat()
        val h = bitmap.height.toFloat()
        slideToFloor.setPolyToPoly(
            floatArrayOf(0f, 0f, w, 0f, w, h, 0f, h), 0,
            floatArrayOf(tlX, tlY, trX, trY, brX, brY, blX, blY), 0, 4
        )
    }

    /** Starts a new slide: clears it and lays down the lit field (warm, with soft edges). */
    fun beginSlide(color: Int = LIGHT) {
        bitmap.eraseColor(Color.TRANSPARENT)
        val inset = bitmap.height * 0.06f
        fieldRect.set(inset, inset, bitmap.width - inset, bitmap.height - inset)
        fieldPaint.xfermode = null
        fieldPaint.color = color
        fieldPaint.maskFilter = BlurMaskFilter(inset * 0.9f, BlurMaskFilter.Blur.NORMAL)
        slideCanvas.drawRoundRect(fieldRect, inset, inset, fieldPaint)
        fieldPaint.maskFilter = null
    }

    /** Text written in "marker": cut out of the light. Coordinates are slide pixels. */
    fun cutText(text: String, x: Float, baselineY: Float, size: Float, typeface: Typeface?, align: Paint.Align,
                letterSpacing: Float = 0f, strength: Int = 255) {
        cutPaint.typeface = typeface
        cutPaint.textSize = size
        cutPaint.textAlign = align
        cutPaint.letterSpacing = letterSpacing
        cutPaint.alpha = strength
        slideCanvas.drawText(text, x, baselineY, cutPaint)
    }

    fun cutRect(left: Float, top: Float, right: Float, bottom: Float, strength: Int = 255) {
        cutPaint.alpha = strength
        slideCanvas.drawRect(left, top, right, bottom, cutPaint)
    }

    /** Finishes a slide with dust specks on the acetate. */
    fun endSlide() {
        cutPaint.alpha = 120
        cutPaint.strokeWidth = 2f
        for (s in specks) {
            slideCanvas.drawLine(s[0], s[1], s[0] + s[2], s[1] + s[3], cutPaint)
            slideCanvas.drawCircle(s[0], s[1], 2.5f, cutPaint)
        }
    }

    fun clearOccluders() {
        occluderCount = 0
    }

    fun addOccluder(x: Float, y: Float, radius: Float, height: Float) {
        if (occluderCount == occluders.size) {
            occluders.add(Occluder())
        }
        occluders[occluderCount++].apply {
            this.x = x
            this.y = y
            this.radius = radius
            this.height = height
        }
    }

    /** Ratio by which content shrinks toward the projector on a surface at [height]. */
    private fun liftScale(height: Float): Float = (projectorHeight - height) / projectorHeight

    /**
     * Light on the floor. Skips each occluder's footprint (its top is lit instead, see
     * [paintOnTops]) and the shadow it throws away from the projector. [intensity] is 0..1.
     */
    fun paintOnFloor(g: Graphics, intensity: Float) {
        val canvas = (g as? AndroidGraphics)?.currentCanvas() ?: return
        val alpha = (255 * intensity.coerceIn(0f, 1f)).toInt()
        if (alpha <= 0) {
            return
        }
        canvas.save()
        if (occluderCount > 0) {
            path.reset()
            for (i in 0 until occluderCount) {
                val o = occluders[i]
                val grow = 1f / liftScale(o.height)
                // Sweep from the footprint to where the top's outline lands on the floor.
                for (step in 0..SHADOW_STEPS) {
                    val t = step / SHADOW_STEPS.toFloat()
                    val k = 1f + (grow - 1f) * t
                    path.addCircle(
                        projectorX + (o.x - projectorX) * k, projectorY + (o.y - projectorY) * k,
                        o.radius * k, Path.Direction.CW
                    )
                }
            }
            @Suppress("DEPRECATION")
            canvas.clipPath(path, android.graphics.Region.Op.DIFFERENCE)
        }
        canvas.concat(slideToFloor)
        lightPaint.alpha = alpha
        canvas.drawBitmap(bitmap, 0f, 0f, lightPaint)
        canvas.restore()
    }

    /** Light landing on the tops of the occluders, displaced by their height. */
    fun paintOnTops(g: Graphics, intensity: Float) {
        val canvas = (g as? AndroidGraphics)?.currentCanvas() ?: return
        val alpha = (255 * intensity.coerceIn(0f, 1f)).toInt()
        if (alpha <= 0) {
            return
        }
        for (i in 0 until occluderCount) {
            val o = occluders[i]
            val s = liftScale(o.height)
            canvas.save()
            path.reset()
            path.addCircle(o.x, o.y, o.radius, Path.Direction.CW)
            canvas.clipPath(path)
            // A point on the top shows what would have landed further from the projector.
            canvas.scale(s, s, projectorX, projectorY)
            canvas.concat(slideToFloor)
            // Closer to the lamp, so a touch brighter.
            lightPaint.alpha = minOf(255, (alpha / (s * s)).toInt())
            canvas.drawBitmap(bitmap, 0f, 0f, lightPaint)
            canvas.restore()
        }
    }

    companion object {
        /** Warm halogen white. */
        val LIGHT: Int = Color.argb(150, 255, 236, 200)
        private const val SHADOW_STEPS = 4
    }
}
