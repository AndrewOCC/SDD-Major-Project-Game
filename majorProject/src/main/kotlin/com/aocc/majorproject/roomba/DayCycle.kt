package com.aocc.majorproject.roomba

import android.graphics.Color

/**
 * A level is one afternoon: from 4 pm sunshine through golden hour and sunset to night, when
 * the owner gets home at [ARRIVAL_SECONDS]. Drives the room render crossfades, the clock on
 * the floor projection, how hard the mites push, and the ambient light on sprites.
 */
object DayCycle {

    /** Pre-rendered room states (tools/render/apartment.py --variant). */
    enum class Scene(val asset: String) {
        DAY("room-day.jpg"),
        GOLDEN("room-golden.jpg"),
        SUNSET("room-sunset.jpg"),
        DUSK("room-dusk.jpg"),
        NIGHT("room-night.jpg"),
        DOOR("room-door.jpg"),
        LIGHTS("room-lights.jpg"),
    }

    /** Seconds of play until the key turns in the lock. */
    const val ARRIVAL_SECONDS = 180f

    /** When each scene is fully established, and how long the blend into it takes. */
    private val KEYS = listOf(
        Key(0f, Scene.DAY),
        Key(55f, Scene.GOLDEN),
        Key(100f, Scene.SUNSET),
        Key(138f, Scene.DUSK),
        Key(165f, Scene.NIGHT),
    )
    private const val BLEND_SECONDS = 14f

    private class Key(val at: Float, val scene: Scene)

    /** Background for time [t]: the scene behind, the scene in front, and how far in (0..1). */
    class Blend(var from: Scene = Scene.DAY, var to: Scene = Scene.DAY, var amount: Float = 0f)

    fun blendAt(t: Float, out: Blend): Blend {
        var current = KEYS[0]
        for (key in KEYS) {
            if (t >= key.at) {
                current = key
            }
        }
        val index = KEYS.indexOf(current)
        val next = KEYS.getOrNull(index + 1)
        out.from = current.scene
        out.to = next?.scene ?: current.scene
        out.amount = if (next == null) 0f else ((t - (next.at - BLEND_SECONDS)) / BLEND_SECONDS).coerceIn(0f, 1f)
        return out
    }

    /** 4:00 pm at the start to 8:00 pm on arrival, in five-minute steps. */
    fun clockAt(t: Float): String {
        val minutes = (16 * 60 + (t / ARRIVAL_SECONDS).coerceIn(0f, 1f) * 240).toInt() / 5 * 5
        val hour = (minutes / 60 - 1) % 12 + 1
        return "%d:%02d".format(hour, minutes % 60)
    }

    /** Spawn-rate multiplier: easy afternoon, the mites swarm from sunset. */
    fun spawnBoost(t: Float): Float = interp(t, floatArrayOf(0f, 55f, 100f, 138f, 180f), floatArrayOf(1f, 1.15f, 1.7f, 2.1f, 2.3f))

    /** The projector reads stronger as the room darkens. */
    fun projectorIntensity(t: Float): Float =
        interp(t, floatArrayOf(0f, 55f, 100f, 138f, 165f), floatArrayOf(0.5f, 0.58f, 0.72f, 0.9f, 1f))

    /** Ambient light colour multiplied into sprites so they sit in the render's lighting. */
    fun ambientAt(t: Float): Int {
        val times = floatArrayOf(0f, 55f, 100f, 138f, 165f)
        return Color.rgb(
            interp(t, times, floatArrayOf(255f, 255f, 240f, 150f, 120f)).toInt(),
            interp(t, times, floatArrayOf(255f, 232f, 200f, 160f, 128f)).toInt(),
            interp(t, times, floatArrayOf(255f, 205f, 175f, 215f, 180f)).toInt(),
        )
    }

    val AMBIENT_DOOR: Int = Color.rgb(170, 150, 150)
    val AMBIENT_LIGHTS: Int = Color.rgb(250, 228, 200)

    private fun interp(t: Float, times: FloatArray, values: FloatArray): Float {
        if (t <= times[0]) {
            return values[0]
        }
        for (i in 1 until times.size) {
            if (t <= times[i]) {
                val f = (t - times[i - 1]) / (times[i] - times[i - 1])
                return values[i - 1] + (values[i] - values[i - 1]) * f
            }
        }
        return values.last()
    }
}
