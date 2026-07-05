package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.PersonalMethods
import com.aocc.framework.implementation.RotationHandler
import kotlin.math.PI
import kotlin.math.atan

class Player(private val session: GameSession) {

    val borderWidth = 3f
    private val sensitivity = 100f
    private val maxSpeed = 30
    private val maxRadius = 50
    private val minRadius = 10

    private val maxHealth = 5
    private val maxShield = 100
    private val maxOverheat = 75

    private var firstOverheat = true
    var tiltMode = 2

    var xBias = 0f
    var yBias = -0.3f
    private var health = 5
    private var combo = 0
    private var shield = maxShield

    private var characterDiameter = health * 15f
    private var shieldWidth = shield / 4f

    private var defaultX = 600f
    private var defaultY = 320f

    private var centerX = 0f
    private var centerY = 0f

    private var velocityX = 0f
    private var velocityY = 0f

    private var facingAngle = -90f

    private var mainCharacter = RectF(0f, 0f, 0f, 0f)
    private var overheat = 0
    private var shieldDrainAccumulator = 0f

    /** Remaining invincibility (damage i-frames + flash) while > 0. Movement is unaffected. */
    private var iframeSeconds = 0f

    /** Debug-popup "god mode" toggle: damage is ignored entirely while true. */
    var debugInvincible = false

    fun update(deltaSeconds: Float) {
        val step = GameConstants.secondsToSteps(deltaSeconds)

        if (iframeSeconds > 0f) {
            iframeSeconds = maxOf(0f, iframeSeconds - deltaSeconds)
        }

        velocityX = (PersonalMethods.limitInside(RotationHandler.getRotationX(), -90, 90) / 90f + xBias) * sensitivity
        velocityX = PersonalMethods.limitInside(velocityX, -maxSpeed, maxSpeed)
        velocityY = (PersonalMethods.limitInside(RotationHandler.getRotationY(), -90, 90) / 90f + yBias) * sensitivity
        velocityY = PersonalMethods.limitInside(velocityY, -maxSpeed, maxSpeed)

        facingAngle = if (velocityX > 0) {
            (atan((velocityY / velocityX).toDouble()) * 180 / PI).toFloat()
        } else {
            180f + (atan((velocityY / velocityX).toDouble()) * 180 / PI).toFloat()
        }

        if (defaultX + velocityX * step < borderWidth) {
            velocityX = 0f
            defaultX = borderWidth
        }

        if (defaultY + velocityY * step < GameConstants.PLAY_AREA_TOP) {
            velocityY = 0f
            defaultY = GameConstants.PLAY_AREA_TOP.toFloat()
        }

        if (defaultX + velocityX * step > GameConstants.WORLD_WIDTH - characterDiameter - borderWidth) {
            velocityX = 0f
            defaultX = GameConstants.WORLD_WIDTH - characterDiameter - borderWidth
        }

        if (defaultY + velocityY * step > GameConstants.WORLD_HEIGHT - characterDiameter - borderWidth) {
            velocityY = 0f
            defaultY = GameConstants.WORLD_HEIGHT - characterDiameter - borderWidth
        }

        if (health <= 0) {
            session.setGameOverFlag(true)
        }

        if (shield < maxShield) {
            overheat = (overheat - step).toInt()
        }

        shieldDrainAccumulator += step
        while (shieldDrainAccumulator >= 1f) {
            shield--
            shieldDrainAccumulator -= 1f
        }

        shieldWidth = shield / 2f

        health = PersonalMethods.limitInside(health.toFloat(), 0, maxHealth).toInt()
        shield = PersonalMethods.limitInside(shield.toFloat(), 0, maxShield).toInt()
        overheat = PersonalMethods.limitInside(overheat.toFloat(), 0, maxOverheat).toInt()
        characterDiameter = maxRadius * health / 5f + minRadius

        if (overheat == maxOverheat) {
            combo = 0
            if (firstOverheat) {
                Assets.burn?.play(GamePreferences.getTapVolume().toFloat())
                firstOverheat = false
            }
        } else {
            firstOverheat = true
        }

        defaultX += velocityX * step
        defaultY += velocityY * step

        mainCharacter.set(
            defaultX.toInt().toFloat(),
            defaultY.toInt().toFloat(),
            defaultX.toInt() + characterDiameter,
            defaultY.toInt() + characterDiameter
        )

        centerX = mainCharacter.centerX()
        centerY = mainCharacter.centerY()
    }

    fun paint(g: Graphics, paint: Paint) {
        paint.style = Paint.Style.FILL
        g.drawCircle(centerX, centerY, characterDiameter / 2 + borderWidth, Color.WHITE)
        g.drawCircle(centerX, centerY, characterDiameter / 2 + shieldWidth, Color.argb(150, 0, 0, 255))
        g.drawArc(mainCharacter, facingAngle, 120f, true, Color.BLUE)
        g.drawArc(mainCharacter, facingAngle + 120f, 120f, true, Color.RED)
        g.drawArc(mainCharacter, facingAngle + 240f, 120f, true, Color.GREEN)
        g.drawCircle(centerX, centerY, (characterDiameter / 10), Color.WHITE)
        if (overheat >= maxOverheat) {
            g.drawCircle(
                centerX, centerY,
                characterDiameter / 2 + borderWidth + shieldWidth + 3,
                Color.argb(250, 255, 127, 39)
            )
            g.drawString("Overheating!", 100, 50, Color.MAGENTA, paint)
        } else {
            g.drawCircle(
                centerX, centerY,
                characterDiameter / 2 + borderWidth + shieldWidth + 3,
                Color.argb(150 * overheat / maxOverheat, 255, 127, 39)
            )
        }

        if (iframeSeconds > 0f) {
            // Pulsing red flash while invincible.
            val pulse = (iframeSeconds / IFRAME_SECONDS).coerceIn(0f, 1f)
            g.drawCircle(
                centerX, centerY,
                characterDiameter / 2 + borderWidth + shieldWidth + 8,
                Color.argb((200 * pulse).toInt().coerceIn(0, 255), 255, 60, 60)
            )
        }
    }

    /**
     * Applies a damaging hit: loses one health, resets combo and grants invincibility frames.
     * No-op while already invincible so a single collision can't chain-drain health.
     */
    fun onDamaged() {
        if (debugInvincible || iframeSeconds > 0f) {
            return
        }
        health -= 1
        combo = 0
        iframeSeconds = IFRAME_SECONDS
    }

    fun isInvincible(): Boolean = iframeSeconds > 0f

    fun getCombo(): Int = combo

    fun setCombo(combo: Int) {
        this.combo = combo
    }

    fun getOverheat(): Int = overheat

    fun setOverheat(overheat: Int) {
        this.overheat = overheat
    }

    fun getCharacterDiameter(): Float = characterDiameter

    fun setCharacterDiameter(characterDiameter: Float) {
        this.characterDiameter = characterDiameter
    }

    fun getShieldWidth(): Float = shieldWidth

    fun setShieldWidth(shieldWidth: Float) {
        this.shieldWidth = shieldWidth
    }

    fun getShield(): Int = shield

    fun setShield(shield: Int) {
        this.shield = shield
    }

    fun getShieldRadius(): Float = shieldWidth

    fun setShieldRadius(shieldRadius: Float) {
        shieldWidth = shieldRadius
    }

    fun getDefaultX(): Float = defaultX

    fun getDefaultY(): Float = defaultY

    fun getVelocityX(): Float = velocityX

    fun getVelocityY(): Float = velocityY

    fun setDefaultX(defaultX: Int) {
        this.defaultX = defaultX.toFloat()
    }

    fun setDefaultY(defaultY: Int) {
        this.defaultY = defaultY.toFloat()
    }

    fun setVelocityX(velocityX: Float) {
        this.velocityX = velocityX
    }

    fun setVelocityY(velocityY: Float) {
        this.velocityY = velocityY
    }

    fun getCharacterRadius(): Float = characterDiameter

    fun setCharacterRadius(characterRadius: Int) {
        characterDiameter = characterRadius.toFloat()
    }

    fun setDefaultX(defaultX: Float) {
        this.defaultX = defaultX
    }

    fun setDefaultY(defaultY: Float) {
        this.defaultY = defaultY
    }

    fun getMainCharacter(): RectF = mainCharacter

    fun setMainCharacter(mainCharacter: RectF) {
        this.mainCharacter = mainCharacter
    }

    fun getFacingAngle(): Float = facingAngle

    fun setFacingAngle(facingAngle: Float) {
        this.facingAngle = facingAngle
    }

    fun getCenterX(): Float = centerX

    fun getCenterY(): Float = centerY

    fun setCenterX(centerX: Float) {
        this.centerX = centerX
    }

    fun setCenterY(centerY: Float) {
        this.centerY = centerY
    }

    fun getBORDER_WIDTH(): Float = borderWidth

    fun getHealth(): Int = health

    fun setHealth(health: Int) {
        this.health = health
    }

    fun setCharacterRadius(characterRadius: Float) {
        characterDiameter = characterRadius
    }

    companion object {
        /** Invincibility / flash window applied on taking damage. */
        const val IFRAME_SECONDS = 0.5f
    }
}
