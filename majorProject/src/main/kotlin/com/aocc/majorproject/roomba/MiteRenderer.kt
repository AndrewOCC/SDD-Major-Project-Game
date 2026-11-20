package com.aocc.majorproject.roomba

import android.graphics.Color
import com.aocc.framework.Graphics
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** An evil dust mite: a fuzzy, wobbling blob with legs and red eyes that track the Roomba. */
object MiteRenderer {

    private const val FUZZ_TUFTS = 12

    /**
     * @param age seconds alive (drives wobble and leg wiggle)
     * @param targetX where it is looking (the Roomba)
     * @param opacity 0..1 fade while spawning in
     */
    fun paint(
        g: Graphics,
        x: Float,
        y: Float,
        radius: Float,
        age: Float,
        targetX: Float,
        targetY: Float,
        opacity: Float,
    ) {
        if (radius <= 0.5f) {
            return
        }
        val a = opacity.coerceIn(0f, 1f)
        fun c(color: Int, alpha: Int = 255): Int {
            val lit = RoombaTheme.lit(color)
            return Color.argb((alpha * a).toInt(), Color.red(lit), Color.green(lit), Color.blue(lit))
        }
        // The eyes glow: never dimmed by the ambient light.
        fun glow(color: Int) = Color.argb((255 * a).toInt(), Color.red(color), Color.green(color), Color.blue(color))

        val look = atan2((targetY - y).toDouble(), (targetX - x).toDouble())
        val lookX = cos(look).toFloat()
        val lookY = sin(look).toFloat()
        val sideX = -lookY
        val sideY = lookX

        g.drawCircle(x + 2f, y + radius * 0.35f, radius * 0.95f, c(Color.BLACK, 60))

        // Legs: three per side, scuttling.
        for (side in listOf(-1f, 1f)) {
            for (i in 0 until 3) {
                val along = (i - 1) * radius * 0.55f
                val wiggle = sin(age * 18f + i * 2.1f + side) * radius * 0.25f
                val baseX = x + lookX * along + sideX * side * radius * 0.6f
                val baseY = y + lookY * along + sideY * side * radius * 0.6f
                g.drawStroke(
                    baseX, baseY,
                    baseX + sideX * side * radius * 0.75f + lookX * wiggle,
                    baseY + sideY * side * radius * 0.75f + lookY * wiggle,
                    c(RoombaTheme.MITE_FUZZ), maxOf(1.2f, radius * 0.12f)
                )
            }
        }

        // Fuzz tufts round the rim, gently breathing.
        for (i in 0 until FUZZ_TUFTS) {
            val angle = i * (2 * Math.PI / FUZZ_TUFTS) + age * 0.6
            val breathe = 1f + 0.12f * sin(age * 5f + i * 1.7f)
            g.drawCircle(
                x + (cos(angle) * radius * 0.78f).toFloat(),
                y + (sin(angle) * radius * 0.78f).toFloat(),
                radius * 0.34f * breathe, c(RoombaTheme.MITE_FUZZ)
            )
        }
        g.drawCircle(x, y, radius * 0.82f, c(RoombaTheme.MITE_BODY))
        g.drawCircle(x - lookX * radius * 0.2f, y - lookY * radius * 0.2f, radius * 0.45f, c(RoombaTheme.MITE_BELLY, 150))

        // Eyes toward the target, with angry brows.
        for (side in listOf(-1f, 1f)) {
            val ex = x + lookX * radius * 0.38f + sideX * side * radius * 0.3f
            val ey = y + lookY * radius * 0.38f + sideY * side * radius * 0.3f
            g.drawCircle(ex, ey, radius * 0.24f, c(Color.WHITE))
            g.drawCircle(ex + lookX * radius * 0.08f, ey + lookY * radius * 0.08f, radius * 0.12f, glow(RoombaTheme.MITE_PUPIL))
            g.drawStroke(
                ex - sideX * side * radius * 0.2f + lookX * radius * 0.30f,
                ey - sideY * side * radius * 0.2f + lookY * radius * 0.30f,
                ex + sideX * side * radius * 0.2f + lookX * radius * 0.16f,
                ey + sideY * side * radius * 0.2f + lookY * radius * 0.16f,
                c(Color.rgb(40, 30, 50)), maxOf(1.2f, radius * 0.1f)
            )
        }
    }
}
