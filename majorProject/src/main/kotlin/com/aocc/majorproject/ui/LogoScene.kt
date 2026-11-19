package com.aocc.majorproject.ui

import android.graphics.Color
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.majorproject.Assets
import kotlin.math.sin

/** Title shown on the top display in dual-screen mode: the logo over drifting snow. */
class LogoScene {

    private val particles = ParticleField(GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, count = 70)
    private var time = 0f

    fun update(deltaSeconds: Float) {
        time += deltaSeconds
        particles.update(deltaSeconds)
    }

    fun paint(g: Graphics) {
        val background = Assets.game_bg
        if (background != null) {
            g.drawImage(background, 0, 0)
            g.drawARGB(150, 0, 0, 0)
        } else {
            g.drawRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, Color.BLACK)
        }
        particles.paint(g)

        val logo = Assets.logo ?: return
        val bob = sin(time * 0.9f) * 6f
        g.drawImageTransformed(
            logo,
            GameConstants.WORLD_WIDTH / 2f,
            GameConstants.WORLD_HEIGHT / 2f - 20f + bob,
            1f, 0f, 255
        )
    }
}
