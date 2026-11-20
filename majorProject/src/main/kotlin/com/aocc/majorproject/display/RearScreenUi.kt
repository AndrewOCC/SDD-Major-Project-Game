package com.aocc.majorproject.display

import android.graphics.Paint
import com.aocc.framework.Graphics
import com.aocc.framework.Input.TouchEvent
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A screen that, in dual-screen mode, puts all of its controls on the rear touch display.
 * The rear display has its own portrait world of [RearLayout.WIDTH] x [RearLayout.HEIGHT].
 */
interface RearScreenUi {
    /** Paints this screen's rear controls, in rear-world coordinates. Called on the UI thread. */
    fun paintRear(g: Graphics, paint: Paint)

    /** A tap on the rear display, in rear-world coordinates. Called on the UI thread. */
    fun onRearTap(x: Int, y: Int)
}

/** Portrait world for the rear display (the AYN Thor's 1080x1240 panel at 1.5x). */
object RearLayout {
    const val WIDTH = 720
    const val HEIGHT = 826

    @JvmStatic
    fun centerX(width: Int): Int = (WIDTH - width) / 2
}

/** Hands rear-display taps (UI thread) to a screen's update (game thread). */
class RearTapQueue {
    private val taps = ConcurrentLinkedQueue<TouchEvent>()

    fun add(x: Int, y: Int) {
        taps.add(TouchEvent().apply {
            type = TouchEvent.TOUCH_UP
            this.x = x
            this.y = y
        })
    }

    fun drain(): List<TouchEvent> {
        val drained = ArrayList<TouchEvent>()
        while (true) {
            drained.add(taps.poll() ?: break)
        }
        return drained
    }
}
