package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.aocc.framework.Graphics
import com.aocc.framework.Input
import com.aocc.majorproject.ui.UiBounds
import com.aocc.majorproject.ui.UiText

class Button(
    private var posX: Int,
    private var posY: Int,
    type: Int,
    state: Int,
    private var text: String,
) {

    private var enemyRectF = RectF(0f, 0f, 0f, 0f)
    private var type: Int
    private var state: ButtonState
    private var width = 0
    private var height = 0

    enum class ButtonState {
        active, pressed, inactive
    }

    init {
        this.type = type
        this.state = when (state) {
            0 -> ButtonState.active
            1 -> ButtonState.pressed
            else -> ButtonState.inactive
        }

        when (type) {
            1 -> {
                width = 440
                height = 200
            }
            3 -> {
                width = 64
                height = 64
            }
            4 -> {
                width = 200
                height = 100
            }
        }
    }

    fun update() {
    }

    fun paint(g: Graphics, paint: Paint, player: Player) {
        when (type) {
            3 -> {
                when (text) {
                    "Flat" -> {
                        val icon = if (player.tiltMode == 1) {
                            Assets.tilt_control_flat_2
                        } else {
                            Assets.tilt_control_flat
                        }
                        icon?.let { g.drawImage(it, posX, posY) }
                        UiText.drawLeftOfCenter(
                            g, paint, text, posX + width + 12,
                            posY + width / 2, Color.BLACK
                        )
                    }
                    "Tilted" -> {
                        val icon = if (player.tiltMode == 2) {
                            Assets.tilt_control_tilted_2
                        } else {
                            Assets.tilt_control_tilted
                        }
                        icon?.let { g.drawImage(it, posX, posY) }
                        UiText.drawLeftOfCenter(
                            g, paint, text, posX + width + 12,
                            posY + width / 2, Color.BLACK
                        )
                    }
                    "Custom" -> {
                        if (player.tiltMode == 3) {
                            g.drawCircle(
                                (posX + width / 2).toFloat(),
                                (posY + width / 2).toFloat(),
                                (width / 2 + 5).toFloat(),
                                Color.RED
                            )
                        }
                        Assets.tilt_control_custom?.let { g.drawImage(it, posX, posY) }
                        UiText.drawLeftOfCenter(
                            g, paint, text, posX + width + 12,
                            posY + width / 2, Color.BLACK
                        )
                    }
                }
            }
            4 -> {
                val bounds = UiBounds(posX, posY, width, height)
                g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, Color.DKGRAY)
                UiText.drawInBounds(g, paint, text, bounds, UiText.HAlign.CENTER, Color.WHITE)
            }
        }
    }

    fun touchInBounds(event: Input.TouchEvent): Boolean {
        return event.x > posX && event.x < posX + width - 1
            && event.y > posY && event.y < posY + height - 1
    }

    fun onTap() {
        Assets.tap?.play(GamePreferences.getTapVolume().toFloat())
    }

    fun getPosX(): Int = posX

    fun setPosX(posX: Int) {
        this.posX = posX
    }

    fun getPosY(): Int = posY

    fun setPosY(posY: Int) {
        this.posY = posY
    }

    fun getWidth(): Int = width

    fun setWidth(width: Int) {
        this.width = width
    }

    fun getHeight(): Int = height

    fun setHeight(height: Int) {
        this.height = height
    }

    fun getEnemyRectF(): RectF = enemyRectF

    fun setEnemyRectF(enemyRectF: RectF) {
        this.enemyRectF = enemyRectF
    }

    fun getType(): Int = type

    fun setType(type: Int) {
        this.type = type
    }

    fun getState(): ButtonState = state

    fun setState(state: ButtonState) {
        this.state = state
    }

    fun getText(): String = text

    fun setText(text: String) {
        this.text = text
    }

    fun getBounds(): UiBounds = UiBounds(posX, posY, width, height)
}
