package com.aocc.majorproject.ui

import android.graphics.Color
import com.aocc.framework.Graphics
import com.aocc.majorproject.Assets
import java.util.Random
import kotlin.math.sin

/**
 * Drifting snow in the style of the menu art: frost clumps lifted from the background plus
 * soft white dots. Particles fall slowly with a gentle sway and wrap around the edges.
 */
class ParticleField(
    private val width: Int,
    private val height: Int,
    count: Int,
    seed: Long = 7L,
) {

    private class Particle(
        var x: Float,
        var y: Float,
        val fallSpeed: Float,
        val swayAmplitude: Float,
        val swayRate: Float,
        val phase: Float,
        var rotation: Float,
        val spin: Float,
        val scale: Float,
        val alpha: Int,
        /** Index into [Assets.flakes], or -1 for a plain dot. */
        val flake: Int,
        val dotRadius: Float,
    )

    private val random = Random(seed)
    private var time = 0f
    private val particles = List(count) { spawn(randomY = true) }

    private fun spawn(randomY: Boolean): Particle {
        val isFlake = random.nextFloat() < 0.6f
        val depth = 0.35f + random.nextFloat() * 0.65f // nearer particles are bigger, faster, brighter
        return Particle(
            x = random.nextFloat() * width,
            y = if (randomY) random.nextFloat() * height else -MARGIN,
            fallSpeed = 12f + 38f * depth,
            swayAmplitude = 6f + random.nextFloat() * 18f,
            swayRate = 0.4f + random.nextFloat() * 0.8f,
            phase = random.nextFloat() * 6.28f,
            rotation = random.nextFloat() * 360f,
            spin = (random.nextFloat() - 0.5f) * 40f,
            scale = 0.35f + 0.55f * depth,
            alpha = (70 + 150 * depth).toInt(),
            flake = if (isFlake) random.nextInt(FLAKE_VARIANTS) else -1,
            dotRadius = 2.5f + 7f * depth,
        )
    }

    fun update(deltaSeconds: Float) {
        time += deltaSeconds
        for (p in particles) {
            p.y += p.fallSpeed * deltaSeconds
            p.rotation += p.spin * deltaSeconds
            if (p.y > height + MARGIN) {
                p.y = -MARGIN
                p.x = random.nextFloat() * width
            }
        }
    }

    fun paint(g: Graphics) {
        val flakes = Assets.flakes
        for (p in particles) {
            val x = p.x + sin(time * p.swayRate + p.phase) * p.swayAmplitude
            val image = flakes.getOrNull(p.flake)
            if (image != null) {
                g.drawImageTransformed(image, x, p.y, p.scale, p.rotation, p.alpha)
            } else {
                g.drawCircle(x, p.y, p.dotRadius, Color.argb(p.alpha, 255, 255, 255))
            }
        }
    }

    companion object {
        private const val MARGIN = 80f
        private const val FLAKE_VARIANTS = 4
    }
}
