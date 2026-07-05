package com.aocc.majorproject

import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Paint
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.PersonalMethods
import kotlin.math.hypot

/**
 * A single "dot". Dots spring in from a point to full size over [SPAWN_SECONDS], then
 * move according to their [Movement]:
 *
 *  - [Movement.TRACK]: homes toward the player and is confined to the play area (default).
 *  - [Movement.DRIFT]: travels in a straight line at constant velocity, ignores the player,
 *    and despawns once fully off-screen.
 *  - [Movement.PATH]: follows a prescribed list of waypoints (a shape / trajectory); once the
 *    path is exhausted it continues drifting in its last heading and despawns off-screen.
 */
class Enemy(
    spawnX: Float,
    spawnY: Float,
    t: Int,
    private val session: GameSession,
    movement: Movement = Movement.TRACK,
    driftVx: Float = 0f,
    driftVy: Float = 0f,
    private val path: List<PointF>? = null,
    private val pathSpeed: Float = 4f,
) {

    enum class Movement { TRACK, DRIFT, PATH }

    private val player: Player = session.getPlayer()

    private var posX: Float = spawnX
    private var posY: Float = spawnY

    private var accelX = 0f
    private var velocityX = 0f
    private var accelY = 0f
    private var velocityY = 0f

    private var radius = 0f
    private var topspeed = 2
    private var health = 1

    private var enemyRectF = RectF(0f, 0f, 0f, 0f)

    private var type: Int

    private var currentMovement: Movement =
        if (movement == Movement.PATH && path.isNullOrEmpty()) Movement.DRIFT else movement
    private var driftVelX = driftVx
    private var driftVelY = driftVy
    private var pathIndex = 0

    private var spawnElapsed = 0f
    private var despawned = false

    init {
        type = t
        setType(t)
        if (t == 1) {
            topspeed = 3
            radius = 10f
        } else if (t == 2) {
            topspeed = 2
            radius = 20f
        }
    }

    fun update(deltaSeconds: Float) {
        val step = GameConstants.secondsToSteps(deltaSeconds)

        if (spawnElapsed < SPAWN_SECONDS) {
            spawnElapsed = minOf(SPAWN_SECONDS, spawnElapsed + deltaSeconds)
        }

        when (currentMovement) {
            Movement.TRACK -> stepTrack(step)
            Movement.DRIFT -> stepDrift(step)
            Movement.PATH -> stepPath(step)
        }

        val effectiveRadius = effectiveRadius()
        enemyRectF.set(
            posX - effectiveRadius, posY - effectiveRadius,
            posX + effectiveRadius, posY + effectiveRadius
        )

        handleContact()

        if (currentMovement != Movement.TRACK && isOffScreen()) {
            despawned = true
        }
    }

    private fun stepTrack(step: Float) {
        if (posX < player.getCenterX()) {
            accelX = 0.1f
        }
        if (posY < player.getCenterY()) {
            accelY = 0.1f
        }
        if (posX > player.getCenterX()) {
            accelX = -0.1f
        }
        if (posY > player.getCenterY()) {
            accelY = -0.1f
        }

        velocityX += accelX * step
        velocityY += accelY * step

        if (posX - radius + velocityX * step < 0) {
            velocityX = 0f
            posX = radius
        }
        if (posY - radius + velocityY * step < GameConstants.PLAY_AREA_TOP) {
            velocityY = 0f
            posY = GameConstants.PLAY_AREA_TOP + radius
        }
        if (posX + radius + velocityX * step > GameConstants.WORLD_WIDTH) {
            velocityX = 0f
            posX = GameConstants.WORLD_WIDTH - radius
        }
        if (posY + radius + velocityY * step > GameConstants.WORLD_HEIGHT) {
            velocityY = 0f
            posY = GameConstants.WORLD_HEIGHT - radius
        }

        velocityX = PersonalMethods.limitInside(velocityX, -topspeed, topspeed)
        velocityY = PersonalMethods.limitInside(velocityY, -topspeed, topspeed)

        posX += velocityX * step
        posY += velocityY * step
    }

    private fun stepDrift(step: Float) {
        posX += driftVelX * step
        posY += driftVelY * step
    }

    private fun stepPath(step: Float) {
        val waypoints = path
        if (waypoints == null || pathIndex >= waypoints.size) {
            currentMovement = Movement.DRIFT
            return
        }
        val target = waypoints[pathIndex]
        val dx = target.x - posX
        val dy = target.y - posY
        val distance = hypot(dx, dy)
        val travel = pathSpeed * step

        if (distance <= travel || distance < 1f) {
            posX = target.x
            posY = target.y
            pathIndex++
            if (pathIndex >= waypoints.size) {
                // Exhausted the shape: keep drifting along the final heading and leave.
                if (distance > 0.0001f) {
                    driftVelX = dx / distance * pathSpeed
                    driftVelY = dy / distance * pathSpeed
                }
                currentMovement = Movement.DRIFT
            }
            return
        }

        posX += dx / distance * travel
        posY += dy / distance * travel
    }

    private fun handleContact() {
        if (health <= 0 || spawnScale() < CONTACT_MIN_SCALE) {
            return
        }
        val hit = PersonalMethods.rectFInBounds(
            player.getMainCharacter(), player.getShieldRadius().toInt(), enemyRectF
        )
        if (!hit) {
            return
        }

        health--
        if (player.getShield() > 0) {
            session.addScore(10 * player.getCombo())
            player.setCombo(player.getCombo() + 1)
        } else {
            player.onDamaged()
        }
    }

    private fun isOffScreen(): Boolean {
        val margin = radius + OFF_SCREEN_MARGIN
        return posX < -margin ||
            posX > GameConstants.WORLD_WIDTH + margin ||
            posY < -margin ||
            posY > GameConstants.WORLD_HEIGHT + margin
    }

    /** Eased spring scale (0 → slight overshoot → 1) applied while spawning in. */
    private fun spawnScale(): Float {
        if (spawnElapsed >= SPAWN_SECONDS) {
            return 1f
        }
        val t = spawnElapsed / SPAWN_SECONDS
        val c1 = 1.70158f
        val c3 = c1 + 1f
        val p = t - 1f
        return 1f + c3 * p * p * p + c1 * p * p
    }

    private fun effectiveRadius(): Float = radius * maxOf(0f, spawnScale())

    fun increaseTopSpeed() {
        if (topspeed < 20) {
            topspeed++
        }
    }

    fun paint(g: Graphics, paint: Paint) {
        val scale = maxOf(0f, spawnScale())
        val alpha = (255 * minOf(1f, scale)).toInt().coerceIn(0, 255)
        g.drawCircle(posX, posY, radius * scale, Color.argb(alpha, 255, 255, 255))
    }

    fun isDespawned(): Boolean = despawned

    fun getHealth(): Int = health

    fun setHealth(health: Int) {
        this.health = health
    }

    fun getPosX(): Float = posX

    fun getPosY(): Float = posY

    fun getAccelX(): Float = accelX

    fun getVelocityX(): Float = velocityX

    fun getAccelY(): Float = accelY

    fun getVelocityY(): Float = velocityY

    fun getEnemyRectF(): RectF = enemyRectF

    fun setAccelX(accelX: Float) {
        this.accelX = accelX
    }

    fun setVelocityX(velocityX: Float) {
        this.velocityX = velocityX
    }

    fun setAccelY(accelY: Float) {
        this.accelY = accelY
    }

    fun setVelocityY(velocityY: Float) {
        this.velocityY = velocityY
    }

    fun setEnemyRectF(enemyRectF: RectF) {
        this.enemyRectF = enemyRectF
    }

    fun getType(): Int = type

    fun setType(type: Int) {
        this.type = type
    }

    companion object {
        /** Spawn-in "spring to full size" duration. */
        const val SPAWN_SECONDS = 0.5f
        /** Dots don't deal contact damage/score until mostly grown. */
        private const val CONTACT_MIN_SCALE = 0.6f
        private const val OFF_SCREEN_MARGIN = 60f
    }
}
