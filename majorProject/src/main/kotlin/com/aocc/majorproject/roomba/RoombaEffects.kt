package com.aocc.majorproject.roomba

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.Graphics
import com.aocc.majorproject.ui.UiText
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

/** Short-lived feedback: dust puffs sucked into the Roomba and floating text callouts. */
class RoombaEffects {

    private class Puff(
        var x: Float,
        var y: Float,
        val size: Float,
        var life: Float,
    )

    private class Popup(
        val text: String,
        val x: Float,
        var y: Float,
        val color: Int,
        val textSize: Float,
        var life: Float,
        val duration: Float,
    )

    private val random = Random()
    private val puffs = ArrayList<Puff>()
    private val popups = ArrayList<Popup>()

    /** A mite at ([x], [y]) was vacuumed: a puff of dust streams into the Roomba. */
    fun vacuumed(x: Float, y: Float, points: Int) {
        repeat(9) {
            val angle = random.nextFloat() * 6.283f
            val spread = random.nextFloat() * 14f
            puffs.add(Puff(x + cos(angle) * spread, y + sin(angle) * spread, 2.5f + random.nextFloat() * 4f, 0.45f))
        }
        if (points > 0) {
            popup("+$points", x, y - 14f, Color.WHITE, 28f, 0.8f)
        }
    }

    fun popup(text: String, x: Float, y: Float, color: Int, textSize: Float, duration: Float = 1.4f) {
        popups.add(Popup(text, x, y, color, textSize, duration, duration))
    }

    fun update(deltaSeconds: Float, roombaX: Float, roombaY: Float) {
        val puffIterator = puffs.iterator()
        while (puffIterator.hasNext()) {
            val p = puffIterator.next()
            p.life -= deltaSeconds
            // Drawn in toward the suction.
            val pull = 1f - (p.life / 0.45f).coerceIn(0f, 1f)
            p.x += (roombaX - p.x) * (0.08f + 0.25f * pull)
            p.y += (roombaY - p.y) * (0.08f + 0.25f * pull)
            if (p.life <= 0f) {
                puffIterator.remove()
            }
        }
        val popupIterator = popups.iterator()
        while (popupIterator.hasNext()) {
            val p = popupIterator.next()
            p.life -= deltaSeconds
            p.y -= 34f * deltaSeconds
            if (p.life <= 0f) {
                popupIterator.remove()
            }
        }
    }

    fun paintPuffs(g: Graphics) {
        for (p in puffs) {
            val alpha = (200 * (p.life / 0.45f)).toInt().coerceIn(0, 255)
            g.drawCircle(p.x, p.y, p.size, Color.argb(alpha, 150, 140, 128))
        }
    }

    fun paintPopups(g: Graphics, paint: Paint) {
        val previousSize = paint.textSize
        for (p in popups) {
            val fade = (p.life / p.duration).coerceIn(0f, 1f)
            val alpha = (255 * minOf(1f, fade * 2.5f)).toInt()
            paint.textSize = p.textSize
            UiText.drawCentered(g, paint, p.text, p.x.toInt() + 2, p.y.toInt() + 2, Color.argb(alpha / 2, 0, 0, 0))
            UiText.drawCentered(g, paint, p.text, p.x.toInt(), p.y.toInt(),
                Color.argb(alpha, Color.red(p.color), Color.green(p.color), Color.blue(p.color)))
        }
        paint.textSize = previousSize
    }
}
