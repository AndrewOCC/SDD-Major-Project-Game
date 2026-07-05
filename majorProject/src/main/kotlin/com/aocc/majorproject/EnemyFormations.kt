package com.aocc.majorproject

import android.graphics.PointF
import com.aocc.framework.GameConstants
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

/**
 * Spawns groups of dots in prescribed shapes and trajectories.
 *
 * Formations enter from a screen edge (kept below the HUD band) and either drift straight
 * across as a rigid shape (ring / line) or weave along a sine path. All members spring in
 * from their spawn point to full size. These complement the default single tracking dots.
 */
class EnemyFormations(private val random: Random = Random()) {

    /** Picks and spawns a random formation. */
    fun spawnRandom(controller: EnemyController) {
        when (random.nextInt(3)) {
            0 -> driftRing(controller)
            1 -> driftLine(controller)
            else -> weave(controller)
        }
    }

    /** A ring of dots that drifts across the screen as a rigid shape. */
    fun driftRing(controller: EnemyController, count: Int = 6, ringRadius: Float = 70f) {
        val fromLeft = random.nextBoolean()
        val speed = DRIFT_SPEED_MIN + random.nextFloat() * DRIFT_SPEED_SPAN
        val vx = if (fromLeft) speed else -speed
        val centerX = if (fromLeft) -ringRadius - EDGE_INSET else GameConstants.WORLD_WIDTH + ringRadius + EDGE_INSET
        val centerY = randomPlayY(ringRadius)

        for (i in 0 until count) {
            val angle = 2.0 * Math.PI * i / count
            val x = centerX + (cos(angle) * ringRadius).toFloat()
            val y = centerY + (sin(angle) * ringRadius).toFloat()
            controller.addDrift(x, y, 1, vx, 0f)
        }
    }

    /** A vertical line of dots sweeping horizontally across the screen. */
    fun driftLine(controller: EnemyController, count: Int = 5, spacing: Float = 70f) {
        val fromLeft = random.nextBoolean()
        val speed = DRIFT_SPEED_MIN + random.nextFloat() * DRIFT_SPEED_SPAN
        val vx = if (fromLeft) speed else -speed
        val startX = if (fromLeft) -EDGE_INSET else GameConstants.WORLD_WIDTH + EDGE_INSET
        val span = (count - 1) * spacing
        val top = randomPlayY(span / 2f) - span / 2f

        for (i in 0 until count) {
            controller.addDrift(startX, top + i * spacing, 1, vx, 0f)
        }
    }

    /** A train of dots weaving along a sine trajectory. */
    fun weave(controller: EnemyController, count: Int = 4) {
        val fromLeft = random.nextBoolean()
        val dir = if (fromLeft) 1f else -1f
        val baseY = randomPlayY(WAVE_AMPLITUDE)
        val startX = if (fromLeft) -EDGE_INSET else GameConstants.WORLD_WIDTH + EDGE_INSET
        val speed = DRIFT_SPEED_MIN + random.nextFloat() * DRIFT_SPEED_SPAN

        for (i in 0 until count) {
            val memberStartX = startX - dir * i * WAVE_MEMBER_SPACING
            val waypoints = buildWavePath(memberStartX, baseY, dir)
            controller.addPath(memberStartX, baseY, 1, waypoints, speed)
        }
    }

    private fun buildWavePath(startX: Float, baseY: Float, dir: Float): List<PointF> {
        val points = ArrayList<PointF>()
        val endX = if (dir > 0f) {
            GameConstants.WORLD_WIDTH + WAVE_STEP
        } else {
            -WAVE_STEP
        }
        var x = startX
        var phase = 0f
        val minY = GameConstants.PLAY_AREA_TOP + WAVE_AMPLITUDE + 10f
        val maxY = GameConstants.WORLD_HEIGHT - WAVE_AMPLITUDE - 10f
        val clampedBase = baseY.coerceIn(minY, maxY)

        while ((dir > 0f && x < endX) || (dir < 0f && x > endX)) {
            val y = clampedBase + WAVE_AMPLITUDE * sin(phase.toDouble()).toFloat()
            points.add(PointF(x, y))
            x += dir * WAVE_STEP
            phase += WAVE_PHASE_STEP
        }
        return points
    }

    private fun randomPlayY(margin: Float): Float {
        val top = (GameConstants.PLAY_AREA_TOP + margin + 20f).toInt()
        val bottom = (GameConstants.WORLD_HEIGHT - margin - 20f).toInt()
        if (bottom <= top) {
            return ((GameConstants.PLAY_AREA_TOP + GameConstants.WORLD_HEIGHT) / 2).toFloat()
        }
        return (random.nextInt(bottom - top) + top).toFloat()
    }

    companion object {
        private const val DRIFT_SPEED_MIN = 3f
        private const val DRIFT_SPEED_SPAN = 1.5f
        private const val EDGE_INSET = 60f
        private const val WAVE_AMPLITUDE = 90f
        private const val WAVE_STEP = 90f
        private const val WAVE_PHASE_STEP = 0.6f
        private const val WAVE_MEMBER_SPACING = 80f
    }
}
