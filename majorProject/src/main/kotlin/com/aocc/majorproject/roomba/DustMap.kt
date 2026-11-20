package com.aocc.majorproject.roomba

import android.graphics.Bitmap
import android.graphics.Color
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.Graphics.ImageFormat
import com.aocc.framework.implementation.AndroidImage
import com.aocc.majorproject.Assets
import java.util.Random
import kotlin.math.floor

/**
 * The dusty floor the Roomba paints clean. A low-resolution grid of dust (one cell per
 * [CELL] world units) is kept in a bitmap and drawn stretched with filtering, so wiped paths
 * get soft edges. Tracks how much of the floor has been cleaned. There is no dust inside the
 * walls or under furniture, so the whole floor can be finished.
 */
class DustMap(seed: Long = 3L, layout: RoomLayout = Assets.roomLayout) {

    val columns = GameConstants.WORLD_WIDTH / CELL
    val rows = GameConstants.WORLD_HEIGHT / CELL

    private val pixels = IntArray(columns * rows)
    private val bitmap = Bitmap.createBitmap(columns, rows, Bitmap.Config.ARGB_8888)
    private val image = AndroidImage(bitmap, ImageFormat.ARGB8888)
    private var dirtyBitmap = true

    private var totalDusty = 0
    private var cleaned = 0

    init {
        val random = Random(seed)
        val clumps = ValueNoise(random, 9, 6)
        val inset = layout.wall / CELL + 1
        val r = Color.red(RoombaTheme.DUST)
        val g = Color.green(RoombaTheme.DUST)
        val b = Color.blue(RoombaTheme.DUST)
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val index = row * columns + col
                if (col < inset || row < inset || col >= columns - inset || row >= rows - inset ||
                    layout.isSolid((col + 0.5f) * CELL, (row + 0.5f) * CELL)
                ) {
                    pixels[index] = Color.TRANSPARENT
                    continue
                }
                // Patchy clumps plus per-cell speckle so the stretched layer reads as dust.
                val clump = clumps.sample(col.toFloat() / columns, row.toFloat() / rows)
                val alpha = (40 + 120 * clump + random.nextInt(50) - 25).toInt().coerceIn(25, 200)
                pixels[index] = Color.argb(alpha, r, g, b)
                totalDusty++
            }
        }
    }

    /** Fraction of the floor cleaned so far, 0..1. */
    fun coverage(): Float = if (totalDusty == 0) 1f else cleaned.toFloat() / totalDusty

    /** Clears dust within [radius] of ([x], [y]) in world units. Returns cells newly cleaned. */
    fun clean(x: Float, y: Float, radius: Float): Int {
        val minCol = floor((x - radius) / CELL).toInt().coerceAtLeast(0)
        val maxCol = floor((x + radius) / CELL).toInt().coerceAtMost(columns - 1)
        val minRow = floor((y - radius) / CELL).toInt().coerceAtLeast(0)
        val maxRow = floor((y + radius) / CELL).toInt().coerceAtMost(rows - 1)
        val radiusSq = radius * radius
        var newlyCleaned = 0
        for (row in minRow..maxRow) {
            val cy = (row + 0.5f) * CELL - y
            for (col in minCol..maxCol) {
                val cx = (col + 0.5f) * CELL - x
                if (cx * cx + cy * cy > radiusSq) {
                    continue
                }
                val index = row * columns + col
                if (pixels[index] != Color.TRANSPARENT) {
                    pixels[index] = Color.TRANSPARENT
                    newlyCleaned++
                }
            }
        }
        if (newlyCleaned > 0) {
            cleaned += newlyCleaned
            dirtyBitmap = true
        }
        return newlyCleaned
    }

    fun paint(g: Graphics) {
        if (dirtyBitmap) {
            bitmap.setPixels(pixels, 0, columns, 0, 0, columns, rows)
            dirtyBitmap = false
        }
        g.drawImageStretched(image, 0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT)
    }

    /** Small smooth value-noise grid for clumpy dust. */
    private class ValueNoise(random: Random, private val gridW: Int, private val gridH: Int) {
        private val values = FloatArray((gridW + 1) * (gridH + 1)) { random.nextFloat() }

        fun sample(u: Float, v: Float): Float {
            val x = u * gridW
            val y = v * gridH
            val x0 = x.toInt().coerceAtMost(gridW - 1)
            val y0 = y.toInt().coerceAtMost(gridH - 1)
            val tx = smooth(x - x0)
            val ty = smooth(y - y0)
            val a = at(x0, y0) + (at(x0 + 1, y0) - at(x0, y0)) * tx
            val b = at(x0, y0 + 1) + (at(x0 + 1, y0 + 1) - at(x0, y0 + 1)) * tx
            return a + (b - a) * ty
        }

        private fun at(x: Int, y: Int) = values[y * (gridW + 1) + x]
        private fun smooth(t: Float) = t * t * (3 - 2 * t)
    }

    companion object {
        const val CELL = 4
    }
}
