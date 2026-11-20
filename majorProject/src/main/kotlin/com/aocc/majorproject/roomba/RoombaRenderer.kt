package com.aocc.majorproject.roomba

import android.graphics.Color
import android.graphics.RectF
import com.aocc.framework.Graphics
import kotlin.math.cos
import kotlin.math.sin

/** Top-down robot vacuum, drawn procedurally. */
object RoombaRenderer {

    private val oval = RectF()
    private const val PROGRESS_BEADS = 28

    /**
     * @param facingDegrees direction of travel (0 = +x, clockwise in screen space)
     * @param suctionRadius extra radius of the suction field beyond the body
     * @param binEmptying 0..1 progress of the dock emptying the bin (0 = not emptying)
     * @param hitFlash 0..1 strength of the damage flash
     * @param suctionLow whether suction is nearly exhausted (LED turns amber)
     */
    fun paint(
        g: Graphics,
        x: Float,
        y: Float,
        radius: Float,
        facingDegrees: Float,
        brushDegrees: Float,
        suctionRadius: Float,
        binEmptying: Float,
        hitFlash: Float,
        suctionLow: Boolean,
    ) {
        // Suction field: soft glow + crisp edge.
        if (suctionRadius > 1f) {
            val outer = radius + suctionRadius
            g.drawCircle(x, y, outer, Color.argb(40, 120, 214, 255))
            g.drawCircleOutline(x, y, outer, Color.argb(150, 150, 225, 255), 2f)
        }

        // Shadow.
        g.drawCircle(x + 3f, y + 5f, radius, Color.argb(80, 0, 0, 0))

        val facing = Math.toRadians(facingDegrees.toDouble())

        // Side brushes poke out at the front corners and spin.
        for (side in listOf(-1, 1)) {
            val angle = facing + side * Math.toRadians(52.0)
            val bx = x + (cos(angle) * radius * 0.86f).toFloat()
            val by = y + (sin(angle) * radius * 0.86f).toFloat()
            for (bristle in 0 until 3) {
                val a = Math.toRadians((brushDegrees * side + bristle * 120f).toDouble())
                g.drawStroke(
                    bx, by,
                    bx + (cos(a) * radius * 0.42f).toFloat(), by + (sin(a) * radius * 0.42f).toFloat(),
                    Color.argb(220, 210, 210, 200), 1.6f
                )
            }
            g.drawCircle(bx, by, radius * 0.08f, Color.rgb(90, 90, 96))
        }

        // Body, then the front bumper band, then the lid on top.
        g.drawCircle(x, y, radius, RoombaTheme.ROOMBA_BODY)
        oval.set(x - radius, y - radius, x + radius, y + radius)
        g.drawArc(oval, facingDegrees - 75f, 150f, true, RoombaTheme.ROOMBA_BUMPER)
        g.drawCircle(x, y, radius * 0.80f, RoombaTheme.ROOMBA_LID)
        g.drawCircleOutline(x, y, radius * 0.80f, Color.argb(90, 255, 255, 255), 1.2f)
        // Specular sheen on the lid.
        g.drawCircle(x - radius * 0.22f, y - radius * 0.26f, radius * 0.38f, Color.argb(22, 255, 255, 255))

        // Centre "clean" button.
        g.drawCircle(x, y, radius * 0.26f, RoombaTheme.ROOMBA_BUTTON)
        g.drawCircleOutline(x, y, radius * 0.26f, Color.argb(140, 200, 200, 200), 1.2f)

        // Status LED toward the front.
        val ledColor = when {
            hitFlash > 0f -> Color.rgb(255, 70, 60)
            suctionLow -> Color.rgb(255, 180, 40)
            else -> Color.rgb(80, 230, 130)
        }
        val lx = x + (cos(facing) * radius * 0.52f).toFloat()
        val ly = y + (sin(facing) * radius * 0.52f).toFloat()
        g.drawCircle(lx, ly, radius * 0.16f, Color.argb(70, Color.red(ledColor), Color.green(ledColor), Color.blue(ledColor)))
        g.drawCircle(lx, ly, radius * 0.08f, ledColor)

        // Bin emptying progress (on the dock).
        if (binEmptying > 0f) {
            val beads = (PROGRESS_BEADS * binEmptying.coerceIn(0f, 1f)).toInt()
            for (i in 0 until beads) {
                val a = Math.toRadians((-90.0 + 360.0 * i / PROGRESS_BEADS))
                g.drawCircle(
                    x + (cos(a) * (radius + 7f)).toFloat(), y + (sin(a) * (radius + 7f)).toFloat(),
                    2.6f, RoombaTheme.BIN_EMPTY
                )
            }
        }

        // Damage flash.
        if (hitFlash > 0f) {
            g.drawCircleOutline(x, y, radius + 4f, Color.argb((220 * hitFlash).toInt().coerceIn(0, 255), 255, 70, 60), 4f)
        }
    }
}
