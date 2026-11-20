package com.aocc.majorproject.roomba

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics.ImageFormat
import com.aocc.framework.Image
import com.aocc.framework.implementation.AndroidImage
import java.util.Random

/**
 * Procedurally drawn apartment room (oak planks, a rug, walls with skirting), rendered once
 * into a 2x bitmap so it is sharp on the Thor and costs a single blit per frame.
 */
object ApartmentFloor {

    private const val SCALE = 2
    const val RUG_LEFT = 360f
    const val RUG_TOP = 230f
    const val RUG_RIGHT = 920f
    const val RUG_BOTTOM = 560f

    fun build(seed: Long = 42L): Image {
        val width = GameConstants.WORLD_WIDTH
        val height = GameConstants.WORLD_HEIGHT
        val bitmap = Bitmap.createBitmap(width * SCALE, height * SCALE, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.scale(SCALE.toFloat(), SCALE.toFloat())
        val random = Random(seed)
        drawPlanks(canvas, width, height, random)
        drawRug(canvas)
        drawWalls(canvas, width, height)
        return AndroidImage(bitmap, ImageFormat.RGB565, SCALE)
    }

    private fun drawPlanks(canvas: Canvas, width: Int, height: Int, random: Random) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        val seam = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = RoombaTheme.WOOD_SEAM; strokeWidth = 1.2f }
        val grain = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        val plankHeight = 46f
        var y = 0f
        var row = 0
        while (y < height) {
            var x = -random.nextInt(300).toFloat()
            while (x < width) {
                val length = 220f + random.nextInt(220)
                fill.color = jitter(RoombaTheme.WOOD_BASE, random, 14)
                canvas.drawRect(x, y, x + length, y + plankHeight, fill)
                // A few long, gently wavering grain lines per plank.
                for (i in 0 until 3) {
                    grain.color = Color.argb(70 + random.nextInt(50), Color.red(RoombaTheme.WOOD_GRAIN),
                        Color.green(RoombaTheme.WOOD_GRAIN), Color.blue(RoombaTheme.WOOD_GRAIN))
                    val gy = y + 8 + random.nextFloat() * (plankHeight - 16)
                    val path = android.graphics.Path()
                    path.moveTo(x + 6, gy)
                    path.cubicTo(
                        x + length * 0.33f, gy + (random.nextFloat() - 0.5f) * 8,
                        x + length * 0.66f, gy + (random.nextFloat() - 0.5f) * 8,
                        x + length - 6, gy + (random.nextFloat() - 0.5f) * 4
                    )
                    canvas.drawPath(path, grain)
                }
                canvas.drawLine(x + length, y, x + length, y + plankHeight, seam)
                x += length
            }
            canvas.drawLine(0f, y, width.toFloat(), y, seam)
            y += plankHeight
            row++
        }
    }

    private fun drawRug(canvas: Canvas) {
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(60, 0, 0, 0) }
        canvas.drawRoundRect(RectF(RUG_LEFT + 5, RUG_TOP + 7, RUG_RIGHT + 5, RUG_BOTTOM + 7), 10f, 10f, shadow)
        val rug = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = RoombaTheme.RUG_BORDER }
        canvas.drawRoundRect(RectF(RUG_LEFT, RUG_TOP, RUG_RIGHT, RUG_BOTTOM), 10f, 10f, rug)
        rug.color = RoombaTheme.RUG
        val inset = 18f
        canvas.drawRoundRect(RectF(RUG_LEFT + inset, RUG_TOP + inset, RUG_RIGHT - inset, RUG_BOTTOM - inset), 6f, 6f, rug)
        // Diamond lattice.
        val pattern = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RoombaTheme.RUG_PATTERN
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.save()
        canvas.clipRect(RUG_LEFT + inset + 10, RUG_TOP + inset + 10, RUG_RIGHT - inset - 10, RUG_BOTTOM - inset - 10)
        var d = RUG_LEFT - (RUG_BOTTOM - RUG_TOP)
        while (d < RUG_RIGHT) {
            canvas.drawLine(d, RUG_BOTTOM, d + (RUG_BOTTOM - RUG_TOP), RUG_TOP, pattern)
            canvas.drawLine(d, RUG_TOP, d + (RUG_BOTTOM - RUG_TOP), RUG_BOTTOM, pattern)
            d += 56f
        }
        canvas.restore()
        // Fringe.
        val fringe = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = RoombaTheme.RUG_BORDER; strokeWidth = 2f }
        var fy = RUG_TOP + 6
        while (fy < RUG_BOTTOM - 4) {
            canvas.drawLine(RUG_LEFT - 10, fy, RUG_LEFT, fy, fringe)
            canvas.drawLine(RUG_RIGHT, fy, RUG_RIGHT + 10, fy, fringe)
            fy += 7f
        }
    }

    private fun drawWalls(canvas: Canvas, width: Int, height: Int) {
        val t = RoombaTheme.WALL_THICKNESS.toFloat()
        val w = width.toFloat()
        val h = height.toFloat()
        // Soft shadow the walls cast onto the floor.
        val shade = Paint(Paint.ANTI_ALIAS_FLAG)
        val depth = 26f
        shade.shader = LinearGradient(0f, t, 0f, t + depth, Color.argb(90, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        canvas.drawRect(t, t, w - t, t + depth, shade)
        shade.shader = LinearGradient(0f, h - t, 0f, h - t - depth, Color.argb(60, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        canvas.drawRect(t, h - t - depth, w - t, h - t, shade)
        shade.shader = LinearGradient(t, 0f, t + depth, 0f, Color.argb(75, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        canvas.drawRect(t, t, t + depth, h - t, shade)
        shade.shader = LinearGradient(w - t, 0f, w - t - depth, 0f, Color.argb(75, 0, 0, 0), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        canvas.drawRect(w - t - depth, t, w - t, h - t, shade)

        val wall = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = RoombaTheme.WALL }
        canvas.drawRect(0f, 0f, w, t, wall)
        canvas.drawRect(0f, h - t, w, h, wall)
        canvas.drawRect(0f, 0f, t, h, wall)
        canvas.drawRect(w - t, 0f, w, h, wall)

        val skirting = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RoombaTheme.SKIRTING
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(t + 2, t + 2, w - t - 2, h - t - 2, skirting)
        val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = RoombaTheme.WALL_EDGE
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRect(t, t, w - t, h - t, edge)
    }

    private fun jitter(color: Int, random: Random, amount: Int): Int {
        val delta = random.nextInt(amount * 2 + 1) - amount
        return Color.rgb(
            (Color.red(color) + delta).coerceIn(0, 255),
            (Color.green(color) + delta * 3 / 4).coerceIn(0, 255),
            (Color.blue(color) + delta / 2).coerceIn(0, 255)
        )
    }
}
