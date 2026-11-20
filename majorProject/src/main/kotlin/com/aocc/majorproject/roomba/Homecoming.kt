package com.aocc.majorproject.roomba

import android.graphics.Paint
import com.aocc.framework.Graphics
import com.aocc.majorproject.Assets
import com.aocc.majorproject.GamePreferences
import kotlin.math.hypot

/**
 * The end of a level: the owner comes home.
 *
 * Ten seconds out a countdown flicks onto the floor projector while the car pulls up, the
 * door slams, footsteps come up the path, keys jingle and the lock turns. The door swings
 * open and porch light pours in (play continues). Then the light switch clicks on, the mites
 * scatter, and the Roomba trundles back to its dock on its own. The return bonus drains the
 * further and longer it has to go.
 */
class Homecoming {

    enum class Phase { WAITING, COUNTDOWN, DOOR, LIGHTS, RETURNING, DOCKED }

    var phase = Phase.WAITING
        private set
    private var phaseTime = 0f

    /** Seconds of countdown elapsed (0..[COUNTDOWN_SECONDS]). */
    private var countdownTime = 0f
    private val playedCues = HashSet<String>()

    /** Return trip bookkeeping. */
    var returnBonus = RETURN_BONUS_MAX
        private set
    private var lastX = 0f
    private var lastY = 0f

    private val countdownSlide = FloorProjector(
        ProjectedHud.PROJECTOR_X, ProjectedHud.PROJECTOR_Y, ProjectedHud.PROJECTOR_HEIGHT, SLIDE_W, SLIDE_H
    )
    private var shownNumber = -1
    private var pop = 0f

    /** Called each running frame with the level clock; returns true on the frame the lights go on. */
    fun update(deltaSeconds: Float, levelTime: Float): Boolean {
        phaseTime += deltaSeconds
        pop = maxOf(0f, pop - deltaSeconds * 4f)
        when (phase) {
            Phase.WAITING -> if (levelTime >= DayCycle.ARRIVAL_SECONDS - COUNTDOWN_SECONDS) {
                enter(Phase.COUNTDOWN)
                Assets.setMusicVolume(0.3f)
            }
            Phase.COUNTDOWN -> {
                countdownTime += deltaSeconds
                for ((at, cue) in CUES) {
                    if (countdownTime >= at && playedCues.add(cue)) {
                        play(cue)
                    }
                }
                val number = maxOf(1, kotlin.math.ceil(COUNTDOWN_SECONDS - countdownTime).toInt())
                if (number != shownNumber) {
                    shownNumber = number
                    pop = 1f
                    inkNumber(number)
                    play("tick", 0.6f)
                }
                if (countdownTime >= COUNTDOWN_SECONDS) {
                    enter(Phase.DOOR)
                    play("door")
                }
            }
            Phase.DOOR -> if (phaseTime >= DOOR_SECONDS) {
                enter(Phase.LIGHTS)
                play("switch")
                play("scatter", 0.8f)
                return true
            }
            Phase.LIGHTS, Phase.RETURNING, Phase.DOCKED -> Unit
        }
        return false
    }

    /** Starts the trip home from the Roomba's current position. */
    fun beginReturn(x: Float, y: Float) {
        enter(Phase.RETURNING)
        lastX = x
        lastY = y
        returnBonus = RETURN_BONUS_MAX
    }

    /** Drains the bonus by distance driven and time taken; true once it's docked. */
    fun updateReturn(deltaSeconds: Float, x: Float, y: Float, dockX: Float, dockY: Float): Boolean {
        if (phase != Phase.RETURNING) {
            return false
        }
        val travelled = hypot(x - lastX, y - lastY)
        lastX = x
        lastY = y
        returnBonus = maxOf(0f, returnBonus - travelled * BONUS_PER_UNIT - deltaSeconds * BONUS_PER_SECOND)
        if (hypot(x - dockX, y - dockY) < DOCKED_DISTANCE) {
            enter(Phase.DOCKED)
            return true
        }
        return false
    }

    fun secondsInPhase(): Float = phaseTime

    /** 0..1 blend from night to the porch-lit door render. */
    fun doorBlend(): Float = when (phase) {
        Phase.DOOR -> (phaseTime / 0.7f).coerceIn(0f, 1f)
        Phase.LIGHTS, Phase.RETURNING, Phase.DOCKED -> 1f
        else -> 0f
    }

    /** 0..1 blend to the lamps-on render; flickers as the old fluorescent catches. */
    fun lightsBlend(): Float {
        if (phase != Phase.LIGHTS && phase != Phase.RETURNING && phase != Phase.DOCKED) {
            return 0f
        }
        val t = if (phase == Phase.LIGHTS) phaseTime else 1f
        return when {
            t < 0.06f -> 1f
            t < 0.14f -> 0.25f
            t < 0.2f -> 1f
            t < 0.26f -> 0.6f
            else -> 1f
        }
    }

    fun isCountingDown(): Boolean = phase == Phase.COUNTDOWN

    fun isOver(): Boolean = phase == Phase.LIGHTS || phase == Phase.RETURNING || phase == Phase.DOCKED

    fun paintCountdown(g: Graphics, intensity: Float) {
        if (phase != Phase.COUNTDOWN) {
            return
        }
        // Drops in big, then settles: the projector being nudged into focus.
        val scale = 1f + 0.18f * pop * pop
        val cx = 640f
        val cy = 380f
        val hw = SLIDE_W / 2f * scale
        val hh = SLIDE_H / 2f * scale
        // Keystone: wider at the far (lower) edge.
        countdownSlide.setQuad(cx - hw * 0.94f, cy - hh, cx + hw * 0.94f, cy - hh, cx + hw, cy + hh, cx - hw, cy + hh)
        val fade = (countdownTime / 0.35f).coerceIn(0f, 1f)
        countdownSlide.paintOnFloor(g, intensity * fade * (0.85f + 0.3f * pop))
    }

    fun clearOccluders() = countdownSlide.clearOccluders()

    fun addOccluder(x: Float, y: Float, radius: Float, height: Float) = countdownSlide.addOccluder(x, y, radius, height)

    fun paintCountdownTops(g: Graphics, intensity: Float) {
        if (phase == Phase.COUNTDOWN) {
            countdownSlide.paintOnTops(g, intensity)
        }
    }

    private fun inkNumber(number: Int) {
        val w = countdownSlide.slideWidthPx.toFloat()
        val h = countdownSlide.slideHeightPx.toFloat()
        countdownSlide.beginSlide()
        countdownSlide.cutText("HOME IN", w / 2f, h * 0.2f, 44f, Assets.plain, Paint.Align.CENTER, letterSpacing = 0.4f)
        countdownSlide.cutText(number.toString(), w / 2f, h * 0.84f, 330f, Assets.bold ?: Assets.display,
            Paint.Align.CENTER, letterSpacing = -0.04f)
        countdownSlide.endSlide()
    }

    private fun enter(next: Phase) {
        phase = next
        phaseTime = 0f
    }

    private fun play(name: String, gain: Float = 1f) {
        Assets.homeSounds[name]?.play(GamePreferences.getTapVolume() * gain)
    }

    companion object {
        const val COUNTDOWN_SECONDS = 10f
        const val DOOR_SECONDS = 1.6f
        const val RETURN_BONUS_MAX = 2000f
        private const val BONUS_PER_UNIT = 1.1f
        private const val BONUS_PER_SECOND = 70f
        private const val DOCKED_DISTANCE = 14f

        private const val SLIDE_W = 300
        private const val SLIDE_H = 230

        /** Sound cues by seconds into the countdown. */
        private val CUES = listOf(
            0.2f to "car",
            5.6f to "car_door",
            6.4f to "steps",
            8.6f to "keys",
            9.35f to "lock",
        )
    }
}
