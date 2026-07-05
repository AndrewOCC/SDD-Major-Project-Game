package com.aocc.majorproject

import android.graphics.PointF
import com.aocc.framework.GameConstants
import java.util.Random
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Spawns groups of dots in prescribed shapes and trajectories.
 *
 * Formations either drift across the screen as rigid shapes (ring / line / full-height
 * line / box), weave along a sine path, encircle the player, or form an arrow that rotates
 * to keep aiming at the player while spawning and then fires toward it. All members spring
 * in from their spawn point to full size. These complement the default single tracking dots.
 */
class EnemyFormations(private val random: Random = Random()) {

    /** Picks and spawns a random formation. */
    fun spawnRandom(controller: EnemyController, player: Player) {
        when (random.nextInt(7)) {
            0 -> driftRing(controller)
            1 -> driftLine(controller)
            2 -> fullLine(controller)
            3 -> box(controller)
            4 -> circleAroundPlayer(controller, player)
            5 -> aimingArrow(controller, player)
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

    /** A short vertical line of dots sweeping horizontally across the screen. */
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

    /** A full-height wall of dots that sweeps horizontally across the whole screen. */
    fun fullLine(controller: EnemyController, spacing: Float = 64f) {
        val fromLeft = random.nextBoolean()
        val speed = DRIFT_SPEED_MIN + random.nextFloat() * DRIFT_SPEED_SPAN
        val vx = if (fromLeft) speed else -speed
        val startX = if (fromLeft) -EDGE_INSET else GameConstants.WORLD_WIDTH + EDGE_INSET
        val top = GameConstants.PLAY_AREA_TOP + 12f
        val bottom = GameConstants.WORLD_HEIGHT - 12f
        val count = ((bottom - top) / spacing).toInt() + 1

        for (i in 0 until count) {
            controller.addDrift(startX, top + i * spacing, 1, vx, 0f)
        }
    }

    /** A small filled rectangle (grid) of dots drifting across the screen. */
    fun box(controller: EnemyController, cols: Int = 4, rows: Int = 3, spacing: Float = 58f) {
        val fromLeft = random.nextBoolean()
        val speed = DRIFT_SPEED_MIN + random.nextFloat() * DRIFT_SPEED_SPAN
        val vx = if (fromLeft) speed else -speed
        val gridWidth = (cols - 1) * spacing
        val gridHeight = (rows - 1) * spacing
        val leadX = if (fromLeft) -EDGE_INSET else GameConstants.WORLD_WIDTH + EDGE_INSET
        val dirBack = if (fromLeft) -1f else 1f // trailing columns sit behind the lead edge
        val top = randomPlayY(gridHeight / 2f) - gridHeight / 2f

        for (c in 0 until cols) {
            for (rIndex in 0 until rows) {
                val x = leadX + dirBack * c * spacing
                val y = top + rIndex * spacing
                controller.addDrift(x, y, 1, vx, 0f)
            }
        }
    }

    /** A ring of homing dots that spawns encircling the player and closes in. */
    fun circleAroundPlayer(
        controller: EnemyController,
        player: Player,
        count: Int = 8,
        ringRadius: Float = 240f,
    ) {
        val cx = player.getCenterX()
        val cy = player.getCenterY()
        val minY = GameConstants.PLAY_AREA_TOP + 20f
        val maxY = GameConstants.WORLD_HEIGHT - 20f

        for (i in 0 until count) {
            val angle = 2.0 * Math.PI * i / count
            val x = (cx + cos(angle) * ringRadius).toFloat()
                .coerceIn(20f, GameConstants.WORLD_WIDTH - 20f)
            val y = (cy + sin(angle) * ringRadius).toFloat().coerceIn(minY, maxY)
            controller.addTracking(x, y, 1)
        }
    }

    /**
     * An arrowhead of dots that rotates to keep aiming at the player while spawning in,
     * then fires as a group toward the player's position at launch.
     */
    fun aimingArrow(controller: EnemyController, player: Player) {
        val centerX = (100 + random.nextInt(GameConstants.WORLD_WIDTH - 200)).toFloat()
        val centerY = GameConstants.PLAY_AREA_TOP + 50f
        val members = ARROW_OFFSETS.map { controller.addHeld(centerX, centerY, 1) }
        controller.addFormation(
            ArrowFormation(members, ARROW_OFFSETS, centerX, centerY, player, ARROW_LAUNCH_SPEED)
        )
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

        private const val ARROW_LAUNCH_SPEED = 6f
        private const val ARROW_UNIT = 34f

        /**
         * Arrowhead layout in (forward, side) units (scaled by [ARROW_UNIT]); +forward is
         * the aim direction. Tip leads, two wings trail back symmetrically.
         */
        private val ARROW_OFFSETS: List<PointF> = listOf(
            PointF(2f * ARROW_UNIT, 0f),
            PointF(1f * ARROW_UNIT, 0f),
            PointF(0f, 1f * ARROW_UNIT),
            PointF(0f, -1f * ARROW_UNIT),
            PointF(-1f * ARROW_UNIT, 2f * ARROW_UNIT),
            PointF(-1f * ARROW_UNIT, -2f * ARROW_UNIT),
        )
    }
}

/**
 * Keeps an arrowhead of held dots aimed at the player while they spring in, then launches
 * them toward the player's position once the forming window elapses.
 */
private class ArrowFormation(
    private val members: List<Enemy>,
    private val offsets: List<PointF>,
    private val centerX: Float,
    private val centerY: Float,
    private val player: Player,
    private val launchSpeed: Float,
) : ActiveFormation {

    private var elapsed = 0f
    private var released = false

    override fun update(deltaSeconds: Float) {
        if (released) {
            return
        }
        elapsed += deltaSeconds

        val dx = player.getCenterX() - centerX
        val dy = player.getCenterY() - centerY
        val length = hypot(dx, dy).coerceAtLeast(0.0001f)
        val forwardX = dx / length
        val forwardY = dy / length
        val sideX = -forwardY
        val sideY = forwardX

        val limit = minOf(members.size, offsets.size)
        for (i in 0 until limit) {
            val forward = offsets[i].x
            val side = offsets[i].y
            members[i].setPosition(
                centerX + forwardX * forward + sideX * side,
                centerY + forwardY * forward + sideY * side
            )
        }

        if (elapsed >= FORM_SECONDS) {
            for (member in members) {
                member.launch(forwardX * launchSpeed, forwardY * launchSpeed)
            }
            released = true
        }
    }

    override fun isComplete(): Boolean = released

    companion object {
        private val FORM_SECONDS = Enemy.SPAWN_SECONDS
    }
}
