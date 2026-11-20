package com.aocc.majorproject.roomba

import android.graphics.Paint
import com.aocc.framework.Graphics
import com.aocc.majorproject.Assets
import java.util.Random

/**
 * The run's readouts (bin multiplier, score, clean %, clock) on an overhead-projector slide cast
 * onto the open floor between the rug and the credenza. Only re-inks the slide when a value
 * changes. The projector sits up by the windows, so the slide is keystoned wider toward the room.
 */
class ProjectedHud {

    private val projector = FloorProjector(PROJECTOR_X, PROJECTOR_Y, PROJECTOR_HEIGHT, SLIDE_W, SLIDE_H).apply {
        setQuad(398f, 544f, 882f, 544f, 900f, 628f, 380f, 628f)
    }
    private val random = Random()
    private var flicker = 1f
    private var lastKey = ""

    fun update(deltaSeconds: Float) {
        // Old bulb: a faint, uneven shimmer.
        flicker += ((0.94f + random.nextFloat() * 0.06f) - flicker) * minOf(1f, deltaSeconds * 12f)
    }

    fun clearOccluders() = projector.clearOccluders()

    fun addOccluder(x: Float, y: Float, radius: Float, height: Float) = projector.addOccluder(x, y, radius, height)

    /** The last column is the clock ("TIME") normally and the return bonus once the owner is home. */
    fun setValues(score: Int, combo: Int, cleanPercent: Int, lastLabel: String, lastValue: String) {
        val key = "$score|$combo|$cleanPercent|$lastLabel|$lastValue"
        if (key == lastKey) {
            return
        }
        lastKey = key
        val w = projector.slideWidthPx.toFloat()
        projector.beginSlide()
        val label = Assets.plain
        val value = Assets.display ?: Assets.bold
        val labelY = 62f
        val valueY = 156f
        val columns = floatArrayOf(w * 0.12f, w * 0.42f, w * 0.70f, w * 0.89f)
        val labels = arrayOf("BIN", "SCORE", "CLEAN", lastLabel)
        val values = arrayOf("×$combo", formatScore(score), "$cleanPercent%", lastValue)
        val sizes = floatArrayOf(84f, 96f, 84f, 60f)
        for (i in columns.indices) {
            projector.cutText(labels[i], columns[i], labelY, 30f, label, Paint.Align.CENTER, letterSpacing = 0.22f)
            projector.cutText(values[i], columns[i], valueY, sizes[i], value, Paint.Align.CENTER, letterSpacing = -0.01f)
        }
        // Hairline rules between columns, drawn on the acetate with a ruler.
        for (x in floatArrayOf(w * 0.255f, w * 0.585f, w * 0.80f)) {
            projector.cutRect(x - 1.5f, 40f, x + 1.5f, projector.slideHeightPx - 40f, 200)
        }
        projector.endSlide()
    }

    /** Floor pass: draw after the floor and dust, before anything standing on it. */
    fun paintFloor(g: Graphics, intensity: Float) = projector.paintOnFloor(g, intensity * flicker)

    /** Tops pass: draw after the Roomba, mites and dock. */
    fun paintTops(g: Graphics, intensity: Float) = projector.paintOnTops(g, intensity * flicker)

    companion object {
        /** Up by the window wall on a low stand, aimed into the room. */
        const val PROJECTOR_X = 640f
        const val PROJECTOR_Y = 150f
        const val PROJECTOR_HEIGHT = 190f

        private const val SLIDE_W = 520
        private const val SLIDE_H = 96

        fun formatScore(score: Int): String {
            // Thin-space thousands separators read as typeset rather than as a counter.
            val digits = score.toString()
            val sb = StringBuilder()
            for ((i, c) in digits.withIndex()) {
                if (i > 0 && (digits.length - i) % 3 == 0) {
                    sb.append(' ')
                }
                sb.append(c)
            }
            return sb.toString()
        }
    }
}
