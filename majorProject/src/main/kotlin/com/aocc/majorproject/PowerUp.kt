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
    private var outletIndex = -1
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

    /** Plugs the dock into one of the room's outlets (a different one each time), pad facing in. */
    private fun placeOnWall() {
        val layout = Assets.roomLayout
        val outlets = layout.outlets
        var index = r.nextInt(outlets.size)
        if (outlets.size > 1 && index == outletIndex) {
            index = (index + 1 + r.nextInt(outlets.size - 1)) % outlets.size
        }
        outletIndex = index
        val outlet = outlets[index]
        wall = outlet.wall
        val inset = layout.wall + PAD_FROM_WALL
        when (wall) {
            0 -> { posX = inset; posY = outlet.along }
            1 -> { posX = outlet.along; posY = inset }
            2 -> { posX = GameConstants.WORLD_WIDTH - inset; posY = outlet.along }
            else -> { posX = outlet.along; posY = GameConstants.WORLD_HEIGHT - inset }
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
        val charge = if (usable) timeLeft / fullTime.toFloat() else 0f
        val glow = if (usable) (0.6f + 0.4f * sin(pulse * 4f)) else 0f

        val sheet = Assets.dockSheet
        if (sheet != null) {
            g.drawImageRegion(
                sheet, wall * DOCK_CELL, 0, DOCK_CELL, DOCK_CELL,
                x, y, DOCK_CELL.toFloat(), DOCK_CELL.toFloat()
            )
            if (!usable) {
                g.drawCircle(x, y, radius * 0.94f, Color.argb(110, 0, 0, 0))
            }
        } else {
            g.drawCircle(x + 2f, y + 4f, radius.toFloat(), Color.argb(70, 0, 0, 0))
            g.drawCircle(x, y, radius.toFloat(), RoombaTheme.DOCK_BASE)
            g.drawCircle(x, y, radius * 0.86f, RoombaTheme.DOCK_PAD)
            for (side in listOf(-1f, 1f)) {
                g.drawRect((x + side * 16f - 4f).toInt(), (y - 14f).toInt(), 8, 28,
                    if (usable) Color.rgb(210, 180, 90) else Color.rgb(90, 84, 70))
            }
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
        // Status light on the tower, against the wall.
        val lightOffset = radius * 0.86f
        val (lx, ly) = when (wall) {
            0 -> Pair(x - lightOffset, y)
            1 -> Pair(x, y - lightOffset)
            2 -> Pair(x + lightOffset, y)
            else -> Pair(x, y + lightOffset)
        }
        val lightColor = if (usable) RoombaTheme.DOCK_GLOW else Color.rgb(220, 60, 50)
        g.drawCircle(lx, ly, 9f, Color.argb((90 * maxOf(glow, 0.5f)).toInt(), Color.red(lightColor), Color.green(lightColor), Color.blue(lightColor)))
        g.drawCircle(lx, ly, 4f, lightColor)
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

        /** Dock sprite frame size in world units (tools/render/sprites.py DOCK_CELL_UNITS). */
        private const val DOCK_CELL = 128
    }

    fun setUsable(usable: Boolean) {
        this.usable = usable
    }
}
