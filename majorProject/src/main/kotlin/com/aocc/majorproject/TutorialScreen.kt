package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.Input.TouchEvent
import com.aocc.framework.Screen
import com.aocc.majorproject.display.RearLayout
import com.aocc.majorproject.display.RearScreenUi
import com.aocc.majorproject.display.RearTapQueue
import com.aocc.majorproject.input.GamepadInput
import com.aocc.majorproject.roomba.DustMap
import com.aocc.majorproject.roomba.RoombaEffects
import com.aocc.majorproject.roomba.RoombaTheme
import com.aocc.majorproject.ui.UiButton
import com.aocc.majorproject.ui.UiText
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Interactive How to Play. Each lesson drops the real Roomba, dust, mites and dock into the
 * apartment with one goal that exercises a single mechanic. Finishing the goal moves on to the
 * next lesson. Players can step back, skip ahead or leave at any time. In dual-screen mode the
 * instructions and buttons are on the rear touch screen and the top screen is just the room.
 */
class TutorialScreen(private val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame), RearScreenUi {

    enum class Lesson(val title: String, val lines: List<String>) {
        DRIVE("Drive", listOf("Tilt the device to steer.", "Drive through each", "glowing ring.")),
        CLEAN("Clean", listOf("Driving wipes up dust.", "Your blue suction ring", "cleans a wider path.")),
        VACUUM("Vacuum mites", listOf("Touch dust mites with your", "suction ring to vacuum them.", "Each one grows your bin.")),
        RECHARGE("Recharge", listOf("Suction drains as you drive.", "Park on the charging dock", "to refill it.")),
        LINGER("Don't linger", listOf("Stay docked too long and", "it empties your bin.", "Dock, then drive away!")),
        DANGER("No suction", listOf("With no suction, mites", "bite your battery.", "Dodge them!")),
        DONE("You're ready", listOf("Clean the apartment and", "vacuum every mite you can.", "Good luck!")),
    }

    private val session = GameSession()
    private val player = session.getPlayer()
    private val mites = ArrayList<Enemy>()
    private var dock: PowerUp? = null
    private val dust = DustMap(seed = 11L)
    private val effects = RoombaEffects()
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rearPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var lessonIndex = 0
    private var lessonTime = 0f
    private var time = 0f

    /** Seconds since the current goal was met, or < 0 while it is still in progress. */
    private var successTime = -1f

    // Per-lesson progress.
    private var ringsReached = 0
    private var patchStartDust = 1
    private var patchCleaned = 0f
    private var mitesVacuumed = 0
    private var suctionWasLow = false
    private var touchedDock = false
    private var dodgeSeconds = 0f

    private val rearTaps = RearTapQueue()

    // Single-screen controls (top display).
    private val menuButton = UiButton.menuAt(0, 0)
    private val skipButton = UiButton(GameConstants.WORLD_WIDTH - UiButton.MENU_WIDTH, 0, UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT, "Skip")
    private val playButton = UiButton(540, 470, UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT, "Play")

    // Dual-screen controls (rear display, RearLayout coordinates).
    private val rearBackButton = UiButton(40, 560, 310, 120, "Back", 48f)
    private val rearSkipButton = UiButton(370, 560, 310, 120, "Skip", 48f)
    private val rearPlayButton = UiButton(40, 540, 640, 150, "Play", 64f)
    private val rearMenuButton = UiButton(40, 700, 640, 96, "Menu", 42f)

    init {
        GamePreferences.applyTiltTo(player)
        startLesson(0)
    }

    private val lesson: Lesson get() = Lesson.entries[lessonIndex]

    /** Jumps straight to a lesson (also used by screenshot tests). */
    internal fun startLesson(index: Int) {
        lessonIndex = index.coerceIn(0, Lesson.entries.size - 1)
        lessonTime = 0f
        successTime = -1f
        mites.clear()
        dock = null
        session.drainVacuumed()

        player.setDefaultX(START_X - Player.ROOMBA_DIAMETER / 2f)
        player.setDefaultY(START_Y - Player.ROOMBA_DIAMETER / 2f)
        player.setShield(100)
        player.setOverheat(0)
        player.setCombo(0)
        player.setHealth(MAX_HEALTH)
        player.binEmptiedEvent = false

        when (lesson) {
            Lesson.DRIVE -> ringsReached = 0
            Lesson.CLEAN -> {
                patchStartDust = maxOf(1, dust.dustyCellsIn(PATCH.left, PATCH.top, PATCH.right, PATCH.bottom))
                patchCleaned = 0f
            }
            Lesson.VACUUM -> {
                mitesVacuumed = 0
                for ((x, y) in VACUUM_MITES) {
                    mites.add(Enemy(x, y, 2, session, Enemy.Movement.HELD))
                }
            }
            Lesson.RECHARGE -> {
                suctionWasLow = false
                dock = PowerUp(1, session).apply { placeOnWall(BOTTOM_WALL) }
            }
            Lesson.LINGER -> {
                touchedDock = false
                player.setCombo(LINGER_COMBO)
                dock = PowerUp(1, session).apply { placeOnWall(BOTTOM_WALL) }
            }
            Lesson.DANGER -> {
                dodgeSeconds = 0f
                player.setShield(0)
                spawnChaser(0)
                spawnChaser(1)
            }
            Lesson.DONE -> Unit
        }
    }

    private fun spawnChaser(slot: Int) {
        // Opposite corners from wherever the Roomba is.
        val left = player.getCenterX() > GameConstants.WORLD_WIDTH / 2
        val x = if (left xor (slot == 1)) 140f else GameConstants.WORLD_WIDTH - 140f
        val y = if (slot == 0) 160f else GameConstants.WORLD_HEIGHT - 140f
        mites.add(Enemy(x, y, 1, session))
    }

    private fun nextLesson() {
        if (lessonIndex < Lesson.entries.size - 1) {
            startLesson(lessonIndex + 1)
        }
    }

    override fun update(deltaTime: Float) {
        val topTouches = game.input.touchEvents
        val rearTouches = rearTaps.drain()
        if (majorProjectGame.isDualScreenActive()) {
            if (handleRearTouches(rearTouches)) {
                return
            }
        } else if (handleTopTouches(topTouches)) {
            return
        }

        for (action in majorProjectGame.getGamepadInput().consumeActions()) {
            when (action) {
                GamepadInput.Action.CANCEL -> {
                    goToMenu()
                    return
                }
                GamepadInput.Action.CONFIRM -> {
                    playTap()
                    if (lesson == Lesson.DONE) {
                        startGame()
                        return
                    }
                    nextLesson()
                }
                else -> Unit
            }
        }

        time += deltaTime
        lessonTime += deltaTime
        simulate(deltaTime)
    }

    private fun handleTopTouches(events: List<TouchEvent>): Boolean {
        for (event in events) {
            if (event.type != TouchEvent.TOUCH_UP) {
                continue
            }
            if (menuButton.touchInBounds(event)) {
                goToMenu()
                return true
            }
            if (lesson == Lesson.DONE && playButton.touchInBounds(event)) {
                playTap()
                startGame()
                return true
            }
            if (lesson != Lesson.DONE && skipButton.touchInBounds(event)) {
                playTap()
                nextLesson()
            }
        }
        return false
    }

    private fun handleRearTouches(events: List<TouchEvent>): Boolean {
        for (event in events) {
            if (rearMenuButton.touchInBounds(event)) {
                goToMenu()
                return true
            }
            if (lesson == Lesson.DONE) {
                if (rearPlayButton.touchInBounds(event)) {
                    playTap()
                    startGame()
                    return true
                }
                continue
            }
            if (rearBackButton.touchInBounds(event) && lessonIndex > 0) {
                playTap()
                startLesson(lessonIndex - 1)
            } else if (rearSkipButton.touchInBounds(event)) {
                playTap()
                nextLesson()
            }
        }
        return false
    }

    private fun simulate(deltaSeconds: Float) {
        if (lesson == Lesson.DONE) {
            // Free roam on the summary page.
            player.setShield(100)
            player.update(deltaSeconds)
            keepClearOfCard()
            dust.clean(player.getCenterX(), player.getCenterY(), cleanRadius())
            effects.update(deltaSeconds, player.getCenterX(), player.getCenterY())
            return
        }

        // Suction stays full while learning to drive, clean and vacuum.
        if (lesson == Lesson.DRIVE || lesson == Lesson.CLEAN || lesson == Lesson.VACUUM || lesson == Lesson.LINGER) {
            player.setShield(100)
        }
        if (lesson == Lesson.DANGER) {
            player.setShield(0)
        }
        val healthBefore = player.getHealth()
        player.update(deltaSeconds)
        keepClearOfCard()
        dock?.update(deltaSeconds)
        val iterator = mites.iterator()
        while (iterator.hasNext()) {
            val mite = iterator.next()
            mite.update(deltaSeconds)
            if (mite.getHealth() <= 0) {
                iterator.remove()
            }
        }
        dust.clean(player.getCenterX(), player.getCenterY(), cleanRadius())

        for (event in session.drainVacuumed()) {
            effects.vacuumed(event.x, event.y, 0)
            mitesVacuumed++
        }
        if (player.getHealth() < healthBefore) {
            onBitten()
        }
        // Lessons never end the run.
        player.setHealth(MAX_HEALTH)
        session.setGameOverFlag(false)

        if (successTime >= 0f) {
            successTime += deltaSeconds
            if (successTime >= SUCCESS_HOLD_SECONDS) {
                nextLesson()
            }
        } else if (checkGoal(deltaSeconds)) {
            successTime = 0f
            Assets.powerup?.play(GamePreferences.getTapVolume().toFloat())
            effects.popup(SUCCESS_WORDS[lessonIndex % SUCCESS_WORDS.size], player.getCenterX(), player.getCenterY() - 60f,
                RoombaTheme.DOCK_GLOW, 48f, SUCCESS_HOLD_SECONDS)
        }
        effects.update(deltaSeconds, player.getCenterX(), player.getCenterY())
    }

    /** On a single screen the instruction card covers the top strip; keep the Roomba below it. */
    private fun keepClearOfCard() {
        if (!majorProjectGame.isDualScreenActive() && player.getDefaultY() < CARD_HEIGHT + 6f) {
            player.setDefaultY(CARD_HEIGHT + 6f)
            player.setVelocityY(0f)
        }
    }

    private fun cleanRadius(): Float = Player.ROOMBA_DIAMETER / 2f + player.getShieldWidth() * CLEAN_SUCTION_SHARE

    private fun onBitten() {
        if (lesson != Lesson.DANGER) {
            return
        }
        dodgeSeconds = 0f
        effects.popup("Ouch! Start again", player.getCenterX(), player.getCenterY() - 50f, Color.rgb(255, 110, 90), 34f)
        // The mite that bit was used up; send in another.
        spawnChaser(mites.size % 2)
    }

    /** Advances the lesson's goal; returns true the frame it's met. */
    private fun checkGoal(deltaSeconds: Float): Boolean {
        val px = player.getCenterX()
        val py = player.getCenterY()
        return when (lesson) {
            Lesson.DRIVE -> {
                val ring = RINGS[ringsReached]
                if (hypot(px - ring.first, py - ring.second) < RING_RADIUS) {
                    ringsReached++
                    Assets.tap?.play(GamePreferences.getTapVolume().toFloat())
                }
                ringsReached >= RINGS.size
            }
            Lesson.CLEAN -> {
                val left = dust.dustyCellsIn(PATCH.left, PATCH.top, PATCH.right, PATCH.bottom)
                patchCleaned = 1f - left.toFloat() / patchStartDust
                patchCleaned >= PATCH_GOAL
            }
            Lesson.VACUUM -> mites.isEmpty() && lessonTime > 0.5f
            Lesson.RECHARGE -> {
                if (player.getShield() < LOW_SUCTION) {
                    suctionWasLow = true
                }
                suctionWasLow && player.getShield() >= 95
            }
            Lesson.LINGER -> {
                val onDock = isOnDock()
                if (onDock) {
                    touchedDock = true
                }
                if (player.binEmptiedEvent) {
                    player.binEmptiedEvent = false
                    effects.popup("Bin emptied! Try again", px, py - 50f, RoombaTheme.BIN_EMPTY, 34f)
                    player.setCombo(LINGER_COMBO)
                    player.setOverheat(0)
                    touchedDock = false
                    // Back off the dock so the retry starts clean.
                    player.setDefaultX(START_X - Player.ROOMBA_DIAMETER / 2f)
                    player.setDefaultY(START_Y - Player.ROOMBA_DIAMETER / 2f)
                    false
                } else {
                    touchedDock && !onDock && player.getCombo() > 0
                }
            }
            Lesson.DANGER -> {
                dodgeSeconds += deltaSeconds
                if (dodgeSeconds >= DODGE_SECONDS) {
                    mites.clear()
                    true
                } else {
                    false
                }
            }
            Lesson.DONE -> false
        }
    }

    private fun isOnDock(): Boolean {
        val d = dock ?: return false
        return hypot(player.getCenterX() - d.getCenterX(), player.getCenterY() - d.getCenterY()) <
            d.getRadius() + Player.ROOMBA_DIAMETER / 2f
    }

    /** Short progress readout for the current goal. */
    private fun goalText(): String {
        if (successTime >= 0f) {
            return "Done!"
        }
        return when (lesson) {
            Lesson.DRIVE -> "Rings ${ringsReached}/${RINGS.size}"
            Lesson.CLEAN -> "Patch ${(patchCleaned * 100).toInt().coerceAtLeast(0)}% / ${(PATCH_GOAL * 100).toInt()}%"
            Lesson.VACUUM -> "Mites ${mitesVacuumed}/${VACUUM_MITES.size}  -  Bin x${player.getCombo()}"
            Lesson.RECHARGE -> "Suction ${player.getShield()}%"
            Lesson.LINGER -> when {
                isOnDock() -> "Drive off now!"
                else -> "Bin x${player.getCombo()}"
            }
            Lesson.DANGER -> "Dodge ${"%.1f".format(maxOf(0f, DODGE_SECONDS - dodgeSeconds))}s"
            Lesson.DONE -> ""
        }
    }

    private fun startGame() {
        game.setScreen(GameScreen(majorProjectGame))
    }

    private fun goToMenu() {
        playTap()
        game.setScreen(MainMenuScreen(majorProjectGame))
    }

    private fun playTap() {
        Assets.tap?.play(GamePreferences.getTapVolume().toFloat())
    }

    override fun onRearTap(x: Int, y: Int) {
        rearTaps.add(x, y)
    }

    // ---- Painting ------------------------------------------------------------------------

    override fun paint(deltaTime: Float) {
        val g = game.graphics
        RoombaTheme.ambient = Color.WHITE
        Assets.floor?.let { g.drawImage(it, 0, 0) } ?: g.drawRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, Color.BLACK)
        dust.paint(g)
        paintGoalMarkers(g)
        dock?.paint(g, textPaint)
        effects.paintPuffs(g)
        for (mite in mites) {
            mite.paint(g, textPaint)
        }
        player.paint(g, textPaint)
        textPaint.typeface = Assets.plain
        effects.paintPopups(g, textPaint)

        if (!majorProjectGame.isDualScreenActive()) {
            paintTopCard(g)
        }
        VersionOverlay.paint(g)
    }

    private fun pulse(speed: Float = 4f): Float = 0.5f + 0.5f * sin(time * speed)

    private fun paintGoalMarkers(g: Graphics) {
        val glow = RoombaTheme.DOCK_GLOW
        when (lesson) {
            Lesson.DRIVE -> {
                for ((i, ring) in RINGS.withIndex()) {
                    if (i < ringsReached) {
                        continue
                    }
                    val active = i == ringsReached
                    val alpha = if (active) (150 + 100 * pulse()).toInt() else 70
                    val r = RING_RADIUS + if (active) 4f * pulse() else 0f
                    g.drawCircle(ring.first, ring.second, r, Color.argb(alpha / 4, Color.red(glow), Color.green(glow), Color.blue(glow)))
                    g.drawCircleOutline(ring.first, ring.second, r, Color.argb(alpha, Color.red(glow), Color.green(glow), Color.blue(glow)), 5f)
                    textPaint.typeface = Assets.plain
                    textPaint.textSize = 30f
                    UiText.drawCentered(g, textPaint, "${i + 1}", ring.first.toInt(), ring.second.toInt(), Color.argb(alpha, 255, 255, 255))
                }
            }
            Lesson.CLEAN -> {
                val alpha = (120 + 110 * pulse(3f)).toInt()
                g.drawRectOutline(PATCH.left.toInt(), PATCH.top.toInt(), PATCH.width().toInt(), PATCH.height().toInt(),
                    Color.argb(alpha, Color.red(glow), Color.green(glow), Color.blue(glow)), 4f)
            }
            Lesson.RECHARGE, Lesson.LINGER -> dock?.let { d ->
                if (!isOnDock()) {
                    val r = d.getRadius() + 10f + 8f * pulse(5f)
                    g.drawCircleOutline(d.getCenterX(), d.getCenterY(), r,
                        Color.argb((200 * pulse(5f)).toInt() + 40, Color.red(glow), Color.green(glow), Color.blue(glow)), 4f)
                }
            }
            else -> Unit
        }
    }

    /** Single-screen instruction card along the top, with Menu / Skip either side. */
    private fun paintTopCard(g: Graphics) {
        g.drawRect(0, 0, GameConstants.WORLD_WIDTH, CARD_HEIGHT, Color.argb(190, 16, 14, 12))
        textPaint.typeface = Assets.plain
        textPaint.textSize = 34f
        val step = "${lessonIndex + 1}/${Lesson.entries.size}"
        UiText.drawCentered(g, textPaint, "${lesson.title}  ($step)", 640, 24, Color.WHITE)
        textPaint.textSize = 22f
        UiText.drawCentered(g, textPaint, lesson.lines.joinToString(" "), 640, 64, Color.rgb(225, 220, 210))
        textPaint.textSize = 28f
        UiText.drawCentered(g, textPaint, goalText(), 640, 98, RoombaTheme.DOCK_GLOW)

        menuButton.paint(g)
        if (lesson == Lesson.DONE) {
            playButton.paint(g)
        } else {
            skipButton.paint(g)
        }
    }

    override fun paintRear(g: Graphics, paint: Paint) {
        rearPaint.typeface = Assets.plain
        val cx = RearLayout.WIDTH / 2

        // Step dots.
        val count = Lesson.entries.size
        val spacing = 44
        val startX = cx - spacing * (count - 1) / 2
        for (i in 0 until count) {
            val color = when {
                i == lessonIndex -> RoombaTheme.DOCK_GLOW
                i < lessonIndex -> Color.rgb(150, 150, 150)
                else -> Color.rgb(70, 70, 70)
            }
            g.drawCircle((startX + i * spacing).toFloat(), 56f, if (i == lessonIndex) 12f else 9f, color)
        }

        rearPaint.textSize = 30f
        UiText.drawCentered(g, rearPaint, "How to Play  ${lessonIndex + 1}/$count", cx, 110, Color.rgb(170, 170, 170))
        rearPaint.textSize = 62f
        UiText.drawCentered(g, rearPaint, lesson.title, cx, 180, Color.WHITE)
        rearPaint.textSize = 36f
        for ((i, line) in lesson.lines.withIndex()) {
            UiText.drawCentered(g, rearPaint, line, cx, 262 + i * 50, Color.rgb(225, 220, 210))
        }

        if (lesson == Lesson.DONE) {
            rearPlayButton.paint(g)
        } else {
            g.drawRect(40, 432, 640, 96, Color.argb(60, 255, 255, 255))
            rearPaint.textSize = 46f
            UiText.drawCentered(g, rearPaint, goalText(), cx, 480,
                if (successTime >= 0f) Color.WHITE else RoombaTheme.DOCK_GLOW)
            if (lessonIndex > 0) {
                rearBackButton.paint(g)
            }
            rearSkipButton.paint(g)
        }
        rearMenuButton.paint(g)
    }

    override fun backButton() {
        game.setScreen(MainMenuScreen(majorProjectGame))
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
    }

    companion object {
        private const val START_X = 640f
        private const val START_Y = 400f
        private const val MAX_HEALTH = 5
        private const val CARD_HEIGHT = 120
        private const val BOTTOM_WALL = 3
        private const val SUCCESS_HOLD_SECONDS = 1.4f
        private const val CLEAN_SUCTION_SHARE = 0.6f

        private const val RING_RADIUS = 46f
        private val RINGS = listOf(320f to 330f, 960f to 300f, 700f to 590f)

        /** On the rug, clear of furniture. */
        private val PATCH = RectF(470f, 260f, 810f, 500f)
        private const val PATCH_GOAL = 0.85f

        private val VACUUM_MITES = listOf(420f to 300f, 520f to 220f, 760f to 220f, 860f to 300f, 640f to 560f)

        private const val LOW_SUCTION = 30
        private const val LINGER_COMBO = 5
        private const val DODGE_SECONDS = 6f

        private val SUCCESS_WORDS = listOf("Nice!", "Spotless!", "Gotcha!", "Charged!", "Smooth!", "Phew!")
    }
}
