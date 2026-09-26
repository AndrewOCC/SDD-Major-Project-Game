package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.PersonalMethods

class Enemy(
    x: Float,
    y: Float,
    t: Int,
    private val session: GameSession,
) {

    private val player: Player = session.getPlayer()

    private var posX: Float
    private var posY: Float

    private var accelX = 0f
    private var velocityX = 0f
    private var accelY = 0f
    private var velocityY = 0f

    private var radius = 0f
    private var topspeed = 2
    private var health = 1

    private var enemyRectF = RectF(0f, 0f, 0f, 0f)

    private var type: Int

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
        posX = x
        posY = y
    }

    fun update(deltaSeconds: Float) {
        val step = GameConstants.secondsToSteps(deltaSeconds)

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

        if (posY - radius + velocityY * step < 0) {
            velocityY = 0f
            posY = radius
        }

        if (posX + radius + velocityX * step > GameConstants.WORLD_WIDTH) {
            velocityX = 0f
            posX = GameConstants.WORLD_WIDTH - radius
        }

        if (posY + radius + velocityY * step > GameConstants.WORLD_HEIGHT) {
            velocityY = 0f
            posY = GameConstants.WORLD_HEIGHT - radius
        }

        if (PersonalMethods.rectFInBounds(player.getMainCharacter(), player.getShieldRadius().toInt(), enemyRectF)
            && health > 0
        ) {
            health--

            if (player.getShield() > 0) {
                session.addScore(10 * player.getCombo())
                player.setCombo(player.getCombo() + 1)
            } else {
                player.setHealth(player.getHealth() - 1)
                player.setCombo(0)
            }
        }

        velocityX = PersonalMethods.limitInside(velocityX, -topspeed, topspeed)
        velocityY = PersonalMethods.limitInside(velocityY, -topspeed, topspeed)

        posX += velocityX * step
        posY += velocityY * step

        enemyRectF.set(posX - radius, posY - radius, posX + radius, posY + radius)
    }

    fun increaseTopSpeed() {
        if (topspeed < 20) {
            topspeed++
        }
    }

    fun paint(g: Graphics, paint: Paint) {
        g.drawCircle(posX, posY, radius, Color.WHITE)
    }

    fun getHealth(): Int = health

    fun setHealth(health: Int) {
        this.health = health
    }

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
}
