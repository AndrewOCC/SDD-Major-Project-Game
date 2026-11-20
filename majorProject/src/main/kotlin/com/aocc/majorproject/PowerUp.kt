package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.PersonalMethods
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import com.aocc.majorproject.roomba.RoombaTheme

/**
 * The charging dock: always against a wall. Docking refills suction, but stay too long and
 * it empties your bin (the combo). It runs out of charge, goes offline, then moves outlets.
 */
class PowerUp(t: Int, session: GameSession) {

    private var fullTime = 200
    private val minTime = 20
    private var type: Int
    private var radius: Int
    private var posX: Int
    private var posY: Int
    private var timeLeft: Int
    private var usable: Boolean
    private var firstContact = true

    var powerUpRectF: RectF
    private val r = Random()
    private val player: Player = session.getPlayer()

    /** Which wall the dock is plugged into: 0 left, 1 top, 2 right, 3 bottom. */
    private var wall = 0
    private var pulse = 0f

    private var timeDrainAccumulator = 0f
    private var respawnAccumulator = 0f

    init {
        type = t
        powerUpRectF = RectF()
        usable = true
        radius = 50
        posX = 0
        posY = 0
        placeOnWall()
        timeLeft = fullTime
    }

    fun update(deltaSeconds: Float) {
        val step = GameConstants.secondsToSteps(deltaSeconds)
        pulse += deltaSeconds
        if (PersonalMethods.rectFInBounds(player.getMainCharacter(), 0, powerUpRectF) && usable) {
            if (firstContact) {
                Assets.powerup?.play(GamePreferences.getTapVolume().toFloat())
                firstContact = false
            }

            player.setShield(100)
            timeDrainAccumulator += step
            while (timeDrainAccumulator >= 1f) {
                timeLeft--
                timeDrainAccumulator -= 1f
            }
            player.setOverheat(player.getOverheat() + (2f * step).roundToInt())

            if (timeLeft <= 0) {
                usable = false
                if (fullTime > minTime) {
                    fullTime -= 2
                }
            }
        } else {
            firstContact = true
        }

        if (!usable) {
            respawnAccumulator += 2f * step
            while (respawnAccumulator >= 1f) {
                timeLeft++
                respawnAccumulator -= 1f
            }
            if (timeLeft > fullTime) {
                placeOnWall()
                timeLeft = fullTime
                usable = true
            }
        }
    }

    /** Plugs the dock into a random spot along a random wall, pad facing into the room. */
    private fun placeOnWall() {
        wall = r.nextInt(4)
        val inset = RoombaTheme.WALL_THICKNESS + PAD_FROM_WALL
        val minX = inset + 40
        val maxX = GameConstants.WORLD_WIDTH - inset - 40
        val minY = maxOf(GameConstants.PLAY_AREA_TOP + radius, inset + 40)
        val maxY = GameConstants.WORLD_HEIGHT - inset - 40
        when (wall) {
            0 -> { posX = inset; posY = minY + r.nextInt(maxY - minY) }
            1 -> { posX = minX + r.nextInt(maxX - minX); posY = maxOf(inset, GameConstants.PLAY_AREA_TOP + radius) }
            2 -> { posX = GameConstants.WORLD_WIDTH - inset; posY = minY + r.nextInt(maxY - minY) }
            else -> { posX = minX + r.nextInt(maxX - minX); posY = GameConstants.WORLD_HEIGHT - inset }
        }
        powerUpRectF.set(
            (posX - radius).toFloat(),
            (posY - radius).toFloat(),
            (posX + radius).toFloat(),
            (posY + radius).toFloat()
        )
    }

    fun paint(g: Graphics, paint: Paint) {
        val x = posX.toFloat()
        val y = posY.toFloat()
        val t = RoombaTheme.WALL_THICKNESS.toFloat()
        val w = GameConstants.WORLD_WIDTH.toFloat()
        val h = GameConstants.WORLD_HEIGHT.toFloat()

        // Base plate running from the wall to under the pad.
        val half = 34
        when (wall) {
            0 -> g.drawRect(t.toInt(), posY - half, (x - t).toInt(), half * 2, RoombaTheme.DOCK_BASE)
            1 -> g.drawRect(posX - half, t.toInt(), half * 2, (y - t).toInt(), RoombaTheme.DOCK_BASE)
            2 -> g.drawRect(posX, posY - half, (w - t - x).toInt(), half * 2, RoombaTheme.DOCK_BASE)
            else -> g.drawRect(posX - half, posY, half * 2, (h - t - y).toInt(), RoombaTheme.DOCK_BASE)
        }

        g.drawCircle(x + 2f, y + 4f, radius.toFloat(), Color.argb(70, 0, 0, 0))
        g.drawCircle(x, y, radius.toFloat(), RoombaTheme.DOCK_BASE)
        g.drawCircle(x, y, radius * 0.86f, RoombaTheme.DOCK_PAD)

        val charge = if (usable) timeLeft / fullTime.toFloat() else 0f
        val glow = if (usable) (0.6f + 0.4f * sin(pulse * 4f)) else 0f
        // Charging contacts.
        for (side in listOf(-1f, 1f)) {
            g.drawRect((x + side * 16f - 4f).toInt(), (y - 14f).toInt(), 8, 28,
                if (usable) Color.rgb(210, 180, 90) else Color.rgb(90, 84, 70))
        }
        // Energy ring: beads for the charge left.
        val beads = (ENERGY_BEADS * charge).toInt()
        for (i in 0 until ENERGY_BEADS) {
            val a = Math.toRadians(-90.0 + 360.0 * i / ENERGY_BEADS)
            val lit = i < beads
            g.drawCircle(
                x + (cos(a) * radius * 0.93f).toFloat(), y + (sin(a) * radius * 0.93f).toFloat(), 3f,
                if (lit) Color.argb((150 + 105 * glow).toInt(), 90, 220, 140) else Color.argb(90, 40, 44, 48)
            )
        }
        // Status light.
        g.drawCircle(x, y + radius * 0.5f, 5f, if (usable) RoombaTheme.DOCK_GLOW else Color.rgb(220, 60, 50))
    }

    fun getType(): Int = type

    fun setType(type: Int) {
        this.type = type
    }

    fun getRadius(): Int = radius

    fun setRadius(radius: Int) {
        this.radius = radius
    }

    fun isUsable(): Boolean = usable

    fun getCenterX(): Float = posX.toFloat()

    fun getCenterY(): Float = posY.toFloat()

    companion object {
        private const val PAD_FROM_WALL = 52
        private const val ENERGY_BEADS = 24
    }

    fun setUsable(usable: Boolean) {
        this.usable = usable
    }
}
