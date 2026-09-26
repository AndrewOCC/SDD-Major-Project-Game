package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.PersonalMethods
import java.util.Random
import kotlin.math.roundToInt

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

    private var timeDrainAccumulator = 0f
    private var respawnAccumulator = 0f

    init {
        type = t
        powerUpRectF = RectF()
        usable = true
        radius = 50
        posX = r.nextInt(GameConstants.WORLD_WIDTH)
        posY = r.nextInt(GameConstants.WORLD_HEIGHT)
        powerUpRectF.set(
            (posX - radius).toFloat(),
            (posY - radius).toFloat(),
            (posX + radius).toFloat(),
            (posY + radius).toFloat()
        )
        timeLeft = fullTime
    }

    fun update(deltaSeconds: Float) {
        val step = GameConstants.secondsToSteps(deltaSeconds)
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
                posX = r.nextInt(GameConstants.WORLD_WIDTH - 100) + 50
                posY = r.nextInt(GameConstants.WORLD_HEIGHT - 100) + 50
                powerUpRectF.set(
                    (posX - radius).toFloat(),
                    (posY - radius).toFloat(),
                    (posX + radius).toFloat(),
                    (posY + radius).toFloat()
                )
                timeLeft = fullTime
                usable = true
            }
        }
    }

    fun paint(g: Graphics, paint: Paint) {
        if (usable) {
            g.drawCircle(posX.toFloat(), posY.toFloat(), radius.toFloat(), Color.RED)
            g.drawCircle(posX.toFloat(), posY.toFloat(), radius * timeLeft / fullTime.toFloat(), Color.BLUE)
        }
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

    fun setUsable(usable: Boolean) {
        this.usable = usable
    }
}
