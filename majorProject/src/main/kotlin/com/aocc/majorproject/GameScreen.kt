package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.Input.TouchEvent
import com.aocc.framework.PersonalMethods
import com.aocc.framework.Screen
import com.aocc.majorproject.display.SecondaryDebugAction
import com.aocc.majorproject.display.SecondaryDebugState
import com.aocc.majorproject.display.RearLayout
import com.aocc.majorproject.display.RearScreenUi
import com.aocc.majorproject.display.RearTapQueue
import com.aocc.majorproject.input.GamepadInput
import com.aocc.majorproject.roomba.DustMap
import com.aocc.majorproject.roomba.RoombaEffects
import com.aocc.majorproject.roomba.RoombaTheme
import com.aocc.majorproject.ui.ComboMeter
import com.aocc.majorproject.ui.PauseMenuPanel
import com.aocc.majorproject.ui.ScoreBar
import com.aocc.majorproject.ui.SettingsPanel
import com.aocc.majorproject.ui.SpatialFocusNavigator
import com.aocc.majorproject.ui.UiBanner
import com.aocc.majorproject.ui.UiButton
import com.aocc.majorproject.ui.UiBounds
import com.aocc.majorproject.ui.UiConfirmDialog
import com.aocc.majorproject.ui.UiLayout
import com.aocc.majorproject.ui.UiSelectionHighlight
import com.aocc.majorproject.ui.UiText
import java.util.Random

/**
 * The game itself. In dual-screen mode every control lives on the rear touch display
 * ([RearScreenUi]); the top display shows only the game and its big moments.
 */
class GameScreen(val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame), RearScreenUi {

    enum class GameState {
        Ready, Running, Paused, GameOver
    }

    var state: GameState = GameState.Ready
        private set

    @Volatile
    private var disposed = false

    private val session = GameSession()
    private val player = session.getPlayer()
    private val enemyController = EnemyController(session)
    private val random = Random()
    private val formations = EnemyFormations(random)
    private val powerUp = PowerUp(1, session)
    private val tempEnemyPoint = Point()
    private val dustMap = DustMap()
    private val effects = RoombaEffects()
    private var nextCleanMilestone = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private lateinit var menuButton: UiButton
    private lateinit var startButton: UiButton
    private lateinit var retryButton: UiButton
    private val settingsPanel = SettingsPanel()
    private val pauseMenuPanel = PauseMenuPanel()
    private val quitConfirmDialog = UiConfirmDialog("Quit and return to the menu?", "Yes", "No")

    // Rear (bottom-screen) controls for dual-screen mode, in RearLayout coordinates.
    // No titles: the top display already says "Get Ready" / "Paused".
    private val rearReadyPanel = PauseMenuPanel(portrait = true, title = "", primaryLabel = "Start", secondaryLabel = "Menu")
    private val rearPausePanel = PauseMenuPanel(portrait = true, title = "")
    private val rearQuitDialog = UiConfirmDialog(
        "Quit and return to the menu?", "Yes", "No", RearLayout.WIDTH, REAR_DIALOG_Y
    )
    private val rearPauseButton = UiButton(RearLayout.centerX(REAR_BUTTON_WIDTH), 600, REAR_BUTTON_WIDTH, 130, "Pause", 54f)
    private val rearRetryButton = UiButton(RearLayout.centerX(REAR_BUTTON_WIDTH), 200, REAR_BUTTON_WIDTH, 170, "Retry", 64f)
    private val rearMenuButton = UiButton(RearLayout.centerX(REAR_BUTTON_WIDTH), 400, REAR_BUTTON_WIDTH, 110, "Menu", 44f)
    private val rearLeaderboardButton =
        UiButton(RearLayout.centerX(REAR_BUTTON_WIDTH), 540, REAR_BUTTON_WIDTH, 110, "Leaderboards", 40f)
    private val rearTaps = RearTapQueue()

    private val scoreBar = ScoreBar()
    private val comboMeter = ComboMeter()
    private val promptBanner = UiBanner(34f)
    private val gameOverBanner = UiBanner(100f)
    private val scoreBanner = UiBanner(60f)

    private var facingAngle = 0f
    private var readyFocusIndex = READY_FOCUS_START
    private var rearReadyFocusIndex = PauseMenuPanel.FOCUS_ITEMS.indexOf(PauseMenuPanel.Item.RESUME)
    private var pauseFocusIndex = 0
    private var resumeCountdownSeconds = 0f
    private var showQuitConfirm = false
    private var quitConfirmFocusIndex = 0
    private var gameOverSelection = 0

    init {
        settingsPanel.setGame(majorProjectGame)
        pauseMenuPanel.setGame(majorProjectGame)
        rearReadyPanel.setGame(majorProjectGame)
        rearPausePanel.setGame(majorProjectGame)
        GamePreferences.applyTiltTo(player)

        menuButton = UiButton.menuAt(0, 0)
        startButton = UiButton(
            UiLayout.centerX(UiButton.MENU_WIDTH), PRIMARY_BUTTON_Y,
            UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT, "Start"
        )
        retryButton = UiButton(540, 500, UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT, "Retry")
    }

    override fun update(deltaTime: Float) {
        if (disposed) {
            return
        }

        // In dual-screen mode only the bottom touch screen has controls; top taps are ignored.
        val topTouches = game.input.touchEvents
        val rearTouches = rearTaps.drain()
        val touchEvents = if (isDual()) rearTouches else topTouches
        val gamepadActions = majorProjectGame.getGamepadInput().consumeActions()

        when (state) {
            GameState.Ready -> {
                updateReady(touchEvents)
                handleGamepadReady(gamepadActions)
            }
            GameState.Running -> {
                updateRunning(touchEvents, deltaTime)
                handleGamepadRunning(gamepadActions)
            }
            GameState.Paused -> {
                updatePaused(touchEvents, deltaTime)
                handleGamepadPaused(gamepadActions)
            }
            GameState.GameOver -> {
                updateGameOver(touchEvents)
                handleGamepadGameOver(gamepadActions)
            }
        }
    }

    private fun updateReady(touchEvents: List<TouchEvent>) {
        if (isDual()) {
            for (event in touchEvents) {
                when {
                    rearReadyPanel.getResumeBounds().contains(event) -> {
                        playTap()
                        changeState(GameState.Running)
                        return
                    }
                    rearReadyPanel.getQuitBounds().contains(event) -> {
                        playTap()
                        leaveForMenu()
                        return
                    }
                    else -> rearReadyPanel.handleSettingsTouch(event, player)
                }
            }
            return
        }
        for (event in touchEvents) {
            if (event.type == TouchEvent.TOUCH_UP) {
                if (startButton.touchInBounds(event)) {
                    playTap()
                    changeState(GameState.Running)
                    return
                }
                if (settingsPanel.handleTouch(event, player)) {
                    continue
                }
                playTap()
                changeState(GameState.Running)
            }
        }
    }

    private fun handleGamepadReady(actions: List<GamepadInput.Action>) {
        if (isDual()) {
            handleGamepadRearReady(actions)
            return
        }
        val focusItems = buildReadyFocusBounds()
        for (action in actions) {
            if (action == GamepadInput.Action.CANCEL) {
                leaveForMenu()
                return
            }
            val direction = SpatialFocusNavigator.directionFrom(action)
            if (direction != null) {
                readyFocusIndex = SpatialFocusNavigator.findNext(
                    readyFocusIndex, direction, focusItems
                )
                continue
            }
            if (action == GamepadInput.Action.CONFIRM || action == GamepadInput.Action.PAUSE) {
                activateReadyFocus()
            }
        }
    }

    private fun handleGamepadRearReady(actions: List<GamepadInput.Action>) {
        val focusItems = PauseMenuPanel.FOCUS_ITEMS.map { rearReadyPanel.getItemBounds(it) }
        for (action in actions) {
            if (action == GamepadInput.Action.CANCEL) {
                leaveForMenu()
                return
            }
            val direction = SpatialFocusNavigator.directionFrom(action)
            if (direction != null) {
                rearReadyFocusIndex = SpatialFocusNavigator.findNext(rearReadyFocusIndex, direction, focusItems)
                continue
            }
            if (action == GamepadInput.Action.PAUSE) {
                playTap()
                changeState(GameState.Running)
                return
            }
            if (action == GamepadInput.Action.CONFIRM) {
                when (val item = PauseMenuPanel.FOCUS_ITEMS[rearReadyFocusIndex]) {
                    PauseMenuPanel.Item.RESUME -> {
                        playTap()
                        changeState(GameState.Running)
                        return
                    }
                    PauseMenuPanel.Item.QUIT -> {
                        playTap()
                        leaveForMenu()
                        return
                    }
                    else -> rearReadyPanel.activateSettingsItem(item, player)
                }
            }
        }
    }

    private fun buildReadyFocusBounds(): List<UiBounds> {
        val items = ArrayList<UiBounds>(READY_FOCUS_SETTINGS_OFFSET + SettingsPanel.ITEM_COUNT)
        items.add(startButton.getBounds())
        for (i in 0 until SettingsPanel.ITEM_COUNT) {
            settingsPanel.getItemBounds(i)?.let { items.add(it) }
        }
        return items
    }

    private fun activateReadyFocus() {
        if (readyFocusIndex == READY_FOCUS_START) {
            playTap()
            changeState(GameState.Running)
            return
        }
        settingsPanel.activateFocusIndex(readyFocusIndex - READY_FOCUS_SETTINGS_OFFSET, player)
    }

    private fun handleGamepadRunning(actions: List<GamepadInput.Action>) {
        for (action in actions) {
            if (action == GamepadInput.Action.PAUSE) {
                openPauseMenu()
                return
            }
        }
    }

    private fun updateRunning(touchEvents: List<TouchEvent>, deltaSeconds: Float) {
        val pauseButton = if (isDual()) rearPauseButton else menuButton
        for (event in touchEvents) {
            if (event.type == TouchEvent.TOUCH_UP && pauseButton.touchInBounds(event)) {
                openPauseMenu()
                return
            }
        }

        val step = GameConstants.secondsToSteps(deltaSeconds)

        session.addUpdateCount(step)
        session.addEnemyCounter(step)

        player.update(deltaSeconds)
        enemyController.update(deltaSeconds)
        powerUp.update(deltaSeconds)
        updateCleaning(deltaSeconds)

        if (session.getSpeed() < GameConstants.SPEED_RAMP_MAX &&
            session.getUpdateCount() >= GameConstants.SPEED_RAMP_INTERVAL_FRAMES
        ) {
            session.incrementSpeed()
            session.subtractUpdateCount(GameConstants.SPEED_RAMP_INTERVAL_FRAMES)
            enemyController.increaseEnemyTopSpeed()
        }

        if (session.getEnemyCounter() > enemyController.getNextEnemySpawn()) {
            if (random.nextInt(100) < FORMATION_SPAWN_PERCENT) {
                formations.spawnRandom(enemyController, player)
                enemyController.generateNextEnemy(session.getSpeed())
            } else {
                // Spawn on open floor: inside the walls and not under the furniture.
                val room = Assets.roomLayout
                val spawnLeft = room.wall + 30
                val spawnTop = maxOf(GameConstants.PLAY_AREA_TOP, room.wall) + 20
                val spawnRight = GameConstants.WORLD_WIDTH - room.wall - 30
                val spawnBottom = GameConstants.WORLD_HEIGHT - room.wall - 30
                for (attempt in 0 until SPAWN_ATTEMPTS) {
                    tempEnemyPoint.x = spawnLeft + random.nextInt(spawnRight - spawnLeft)
                    tempEnemyPoint.y = spawnTop + random.nextInt(spawnBottom - spawnTop)
                    PersonalMethods.limitOutside(
                        tempEnemyPoint,
                        player.getCenterX().toInt(),
                        player.getCenterY().toInt(),
                        100
                    )
                    if (!room.overlapsSolid(tempEnemyPoint.x.toFloat(), tempEnemyPoint.y.toFloat(), 24f)) {
                        break
                    }
                }
                if (random.nextInt(10) == 9) {
                    enemyController.addEnemy(tempEnemyPoint.x, tempEnemyPoint.y, 2)
                } else {
                    enemyController.addEnemy(tempEnemyPoint.x, tempEnemyPoint.y, 1)
                }
            }
            session.resetEnemyCounter()
        }

        if (player.getCombo() == 200) {
            majorProjectGame.onAchievementUnlocked(
                majorProjectGame.getString(R.string.achievement_ccccombo)
            )
        }

        majorProjectGame.updateSecondaryDebugState(
            SecondaryDebugState(player.debugInvincible, session.getSpeed())
        )

        if (session.isGameOverFlag()) {
            changeState(GameState.GameOver)
        }
    }

    /** Floor painting, milestone bonuses and vacuum / bin feedback. */
    private fun updateCleaning(deltaSeconds: Float) {
        val x = player.getCenterX()
        val y = player.getCenterY()
        dustMap.clean(x, y, Player.ROOMBA_DIAMETER / 2f + player.getShieldWidth() * CLEAN_SUCTION_SHARE)

        val coverage = dustMap.coverage()
        while (nextCleanMilestone < CLEAN_MILESTONES.size && coverage >= CLEAN_MILESTONES[nextCleanMilestone]) {
            val bonus = CLEAN_BONUSES[nextCleanMilestone]
            session.addScore(bonus)
            val percent = (CLEAN_MILESTONES[nextCleanMilestone] * 100).toInt()
            effects.popup("Floor $percent% clean! +$bonus", GameConstants.WORLD_WIDTH / 2f, 160f,
                RoombaTheme.DOCK_GLOW, 44f, 2.2f)
            Assets.powerup?.play(GamePreferences.getTapVolume().toFloat())
            nextCleanMilestone++
        }

        for (event in session.drainVacuumed()) {
            effects.vacuumed(event.x, event.y, event.points)
        }
        if (player.binEmptiedEvent) {
            player.binEmptiedEvent = false
            effects.popup("Bin emptied!", x, y - 50f, RoombaTheme.BIN_EMPTY, 34f)
        }
        effects.update(deltaSeconds, x, y)
    }

    private fun cleanPercent(): Int = (dustMap.coverage() * 100).toInt()

    private fun openPauseMenu() {
        openPauseMenu(true)
    }

    private fun openPauseMenu(playSound: Boolean) {
        if (playSound) {
            playTap()
        }
        changeState(GameState.Paused)
        if (GamePreferences.music) {
            Assets.setMusicVolume(0.25f)
        }
    }

    private fun updatePaused(touchEvents: List<TouchEvent>, deltaSeconds: Float) {
        val panel = activePausePanel()
        if (showQuitConfirm) {
            handleQuitConfirmTouch(touchEvents)
            return
        }
        if (resumeCountdownSeconds > 0f) {
            tickResumeCountdown(deltaSeconds)
            handleResumeCountdownTouch(touchEvents)
            return
        }
        for (event in touchEvents) {
            if (event.type == TouchEvent.TOUCH_UP) {
                if (panel.getResumeBounds().contains(event)) {
                    activatePauseItem(PauseMenuPanel.Item.RESUME)
                    return
                }
                if (panel.getQuitBounds().contains(event)) {
                    activatePauseItem(PauseMenuPanel.Item.QUIT)
                    return
                }
                panel.handleSettingsTouch(event, player)
            }
        }
    }

    private fun tickResumeCountdown(deltaSeconds: Float) {
        resumeCountdownSeconds = maxOf(0f, resumeCountdownSeconds - deltaSeconds)
        if (resumeCountdownSeconds <= 0f) {
            resumeFromPause()
        }
    }

    private fun handleResumeCountdownTouch(touchEvents: List<TouchEvent>) {
        for (event in touchEvents) {
            if (event.type == TouchEvent.TOUCH_UP && activePausePanel().getResumeBounds().contains(event)) {
                activatePauseItem(PauseMenuPanel.Item.RESUME)
                return
            }
        }
    }

    private fun handleQuitConfirmTouch(touchEvents: List<TouchEvent>) {
        val quitConfirmDialog = activeQuitDialog()
        for (event in touchEvents) {
            if (event.type != TouchEvent.TOUCH_UP) {
                continue
            }
            if (quitConfirmDialog.getConfirmBounds().contains(event)) {
                confirmQuit(true)
                return
            }
            if (quitConfirmDialog.getCancelBounds().contains(event)) {
                confirmQuit(false)
                return
            }
        }
    }

    private fun handleGamepadPaused(actions: List<GamepadInput.Action>) {
        if (showQuitConfirm) {
            handleGamepadQuitConfirm(actions)
            return
        }
        if (resumeCountdownSeconds > 0f) {
            for (action in actions) {
                if (action == GamepadInput.Action.CANCEL || action == GamepadInput.Action.PAUSE) {
                    cancelResumeCountdown()
                }
            }
            return
        }

        val focusItems = buildPauseFocusBounds()
        for (action in actions) {
            val direction = SpatialFocusNavigator.directionFrom(action)
            if (direction != null) {
                pauseFocusIndex = SpatialFocusNavigator.findNext(
                    pauseFocusIndex, direction, focusItems
                )
                continue
            }
            if (action == GamepadInput.Action.CONFIRM) {
                activatePauseFocus()
                continue
            }
            if (action == GamepadInput.Action.CANCEL || action == GamepadInput.Action.PAUSE) {
                // The hardware pause/cancel button is a quick unpause shortcut — the
                // ceremonial countdown only applies to the on-screen Resume button.
                resumeFromPause()
            }
        }
    }

    private fun handleGamepadQuitConfirm(actions: List<GamepadInput.Action>) {
        val quitConfirmDialog = activeQuitDialog()
        val focusItems = listOf(quitConfirmDialog.getConfirmBounds(), quitConfirmDialog.getCancelBounds())
        for (action in actions) {
            val direction = SpatialFocusNavigator.directionFrom(action)
            if (direction != null) {
                quitConfirmFocusIndex = SpatialFocusNavigator.findNext(
                    quitConfirmFocusIndex, direction, focusItems
                )
                continue
            }
            if (action == GamepadInput.Action.CONFIRM) {
                confirmQuit(quitConfirmFocusIndex == 0)
                continue
            }
            if (action == GamepadInput.Action.CANCEL || action == GamepadInput.Action.PAUSE) {
                confirmQuit(false)
            }
        }
    }

    private fun activatePauseFocus() {
        activatePauseItem(PauseMenuPanel.FOCUS_ITEMS[pauseFocusIndex])
    }

    /** Single entry point for activating a pause-menu item, shared by touch and gamepad. */
    private fun activatePauseItem(item: PauseMenuPanel.Item) {
        if (disposed || state != GameState.Paused || showQuitConfirm) {
            return
        }
        if (resumeCountdownSeconds > 0f) {
            if (item == PauseMenuPanel.Item.RESUME) {
                cancelResumeCountdown()
            }
            return
        }
        when (item) {
            PauseMenuPanel.Item.RESUME -> startResumeCountdown()
            PauseMenuPanel.Item.QUIT -> openQuitConfirm()
            else -> pauseMenuPanel.activateSettingsItem(item, player)
        }
    }

    private fun confirmQuit(confirmed: Boolean) {
        if (disposed || state != GameState.Paused || !showQuitConfirm) {
            return
        }
        if (confirmed) {
            playTap()
            leaveForMenu()
        } else {
            playTap()
            showQuitConfirm = false
        }
    }

    /** Invoked by the rear-display debug parameters popup. */
    fun applySecondaryDebugAction(action: SecondaryDebugAction) {
        if (disposed) {
            return
        }
        when (action) {
            is SecondaryDebugAction.ToggleGodMode -> player.debugInvincible = !player.debugInvincible
            is SecondaryDebugAction.SetSpeed -> session.setSpeed(action.speed)
            is SecondaryDebugAction.AddScore -> session.addScore(1000)
        }
    }

    private fun buildPauseFocusBounds(): List<UiBounds> {
        val panel = activePausePanel()
        return PauseMenuPanel.FOCUS_ITEMS.map { panel.getItemBounds(it) }
    }

    private fun startResumeCountdown() {
        playTap()
        resumeCountdownSeconds = RESUME_COUNTDOWN_SECONDS
    }

    private fun cancelResumeCountdown() {
        playTap()
        resumeCountdownSeconds = 0f
    }

    private fun openQuitConfirm() {
        playTap()
        showQuitConfirm = true
        quitConfirmFocusIndex = 0
    }

    private fun resumeFromPause() {
        playTap()
        resumeCountdownSeconds = 0f
        showQuitConfirm = false
        changeState(GameState.Running)
        if (GamePreferences.music) {
            Assets.setMusicVolume(0.85f)
        }
    }

    private fun updateGameOver(touchEvents: List<TouchEvent>) {
        if (!session.isScoreUploaded()) {
            majorProjectGame.onEnteredScore(session.getScore())
            session.setScoreUploaded(true)
        }

        if (isDual()) {
            for (event in touchEvents) {
                val index = buildGameOverFocusBounds().indexOfFirst { it.contains(event) }
                if (index >= 0) {
                    activateGameOverItem(index)
                    return
                }
            }
            return
        }
        for (event in touchEvents) {
            if (event.type == TouchEvent.TOUCH_UP) {
                if (menuButton.touchInBounds(event)) {
                    playTap()
                    leaveForMenu()
                    return
                }
                if (retryButton.touchInBounds(event)) {
                    playTap()
                    restartRun()
                    return
                }
                if (PersonalMethods.touchInBounds(event, 1175, 5, 100, 80)) {
                    playTap()
                    majorProjectGame.onShowLeaderboardsRequested(
                        majorProjectGame.getString(R.string.leaderboard_pacifist_mode)
                    )
                }
            }
        }
    }

    private fun handleGamepadGameOver(actions: List<GamepadInput.Action>) {
        val focusItems = buildGameOverFocusBounds()
        for (action in actions) {
            val direction = SpatialFocusNavigator.directionFrom(action)
            if (direction != null) {
                gameOverSelection = SpatialFocusNavigator.findNext(
                    gameOverSelection, direction, focusItems
                )
                continue
            }
            if (action == GamepadInput.Action.CONFIRM) {
                activateGameOverItem(gameOverSelection)
                continue
            }
            if (action == GamepadInput.Action.CANCEL) {
                playTap()
                leaveForMenu()
            }
        }
    }

    /** Order matters: 0 Menu, 1 Retry, 2 Leaderboards (see [activateGameOverItem]). */
    private fun buildGameOverFocusBounds(): List<UiBounds> {
        if (isDual()) {
            return listOf(rearMenuButton.getBounds(), rearRetryButton.getBounds(), rearLeaderboardButton.getBounds())
        }
        return listOf(
            menuButton.getBounds(),
            retryButton.getBounds(),
            UiBounds(1175, 5, 100, 80)
        )
    }

    private fun activateGameOverItem(index: Int) {
        when (index) {
            0 -> {
                playTap()
                leaveForMenu()
            }
            1 -> {
                playTap()
                restartRun()
            }
            2 -> {
                playTap()
                majorProjectGame.onShowLeaderboardsRequested(
                    majorProjectGame.getString(R.string.leaderboard_pacifist_mode)
                )
            }
        }
    }

    override fun paint(deltaTime: Float) {
        if (disposed) {
            return
        }

        val g = game.graphics
        val floor = Assets.floor
        if (floor != null) {
            g.drawImage(floor, 0, 0)
        } else {
            g.drawRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, Color.BLACK)
        }
        dustMap.paint(g)

        when (state) {
            GameState.Ready -> drawReadyUI(g)
            GameState.Running -> drawRunningUI(g)
            GameState.Paused -> drawPausedUI(g)
            GameState.GameOver -> drawGameOverUI(g)
        }

        VersionOverlay.paint(g)
    }

    private fun drawReadyUI(g: Graphics) {
        g.drawARGB(155, 0, 0, 0)
        if (isDual()) {
            paint.typeface = Assets.plain
            gameOverBanner.paint(g, paint, "Get Ready", GameConstants.WORLD_WIDTH / 2, 320)
            return
        }
        val settingsHighlight = if (readyFocusIndex >= READY_FOCUS_SETTINGS_OFFSET) {
            readyFocusIndex - READY_FOCUS_SETTINGS_OFFSET
        } else {
            -1
        }
        paint.typeface = Assets.plain
        settingsPanel.paint(g, paint, player, settingsHighlight)
        startButton.paint(g)
        if (readyFocusIndex == READY_FOCUS_START) {
            UiSelectionHighlight.paintRect(g, startButton.getBounds())
        }
        paint.typeface = Assets.plain
        promptBanner.paint(g, paint, "Press Start or tap anywhere to begin",
            GameConstants.WORLD_WIDTH / 2, PROMPT_Y)
    }

    private fun drawScene(g: Graphics) {
        powerUp.paint(g, paint)
        effects.paintPuffs(g)
        enemyController.paint(g, paint)
        player.paint(g, paint)
    }

    private fun drawRunningUI(g: Graphics) {
        drawScene(g)
        paint.typeface = Assets.plain
        effects.paintPopups(g, paint)
        // In dual-screen mode the score, combo and pause button live on the bottom screen.
        if (isDual()) {
            return
        }
        comboMeter.paint(g, paint, player.getCombo())
        scoreBar.paint(g, paint, session.getScore())
        paintCleanMeter(g)
        menuButton.paint(g)
    }

    private fun paintCleanMeter(g: Graphics) {
        val x = ScoreBar.BAR_X + ScoreBar.BAR_WIDTH + 8
        g.drawRect(x, 0, CLEAN_METER_WIDTH, ScoreBar.BAR_HEIGHT, Color.argb(100, 255, 255, 255))
        val previousSize = paint.textSize
        paint.textSize = 30f
        UiText.drawCentered(g, paint, "${cleanPercent()}% clean", x + CLEAN_METER_WIDTH / 2,
            ScoreBar.BAR_HEIGHT / 2, Color.WHITE)
        paint.textSize = previousSize
    }

    private fun drawPausedUI(g: Graphics) {
        paint.textSize = 40f
        drawScene(g)
        g.drawARGB(155, 0, 0, 0)
        paint.typeface = Assets.plain

        if (isDual()) {
            gameOverBanner.paint(g, paint, "Paused", GameConstants.WORLD_WIDTH / 2, 320)
            return
        }
        paintPauseOverlay(g, paint)
    }

    /** Pause menu (or quit confirmation), on whichever display currently has the controls. */
    private fun paintPauseOverlay(g: Graphics, paint: Paint) {
        if (showQuitConfirm) {
            activeQuitDialog().paint(g, paint)
            paintQuitConfirmHighlight(g)
            return
        }
        val focusedItem = PauseMenuPanel.FOCUS_ITEMS.getOrNull(pauseFocusIndex)
        activePausePanel().paint(g, paint, player, focusedItem, resumeCountdownSeconds)
    }

    private fun paintQuitConfirmHighlight(g: Graphics) {
        val quitConfirmDialog = activeQuitDialog()
        val bounds = if (quitConfirmFocusIndex == 0) {
            quitConfirmDialog.getConfirmBounds()
        } else {
            quitConfirmDialog.getCancelBounds()
        }
        UiSelectionHighlight.paintRect(g, bounds)
    }

    private fun drawGameOverUI(g: Graphics) {
        g.drawARGB(155, 0, 0, 0)
        if (isDual()) {
            paint.typeface = Assets.plain
            gameOverBanner.paint(g, paint, "Game Over!", GameConstants.WORLD_WIDTH / 2, 260)
            scoreBanner.paint(g, paint, "Score: ${session.getScore()}", GameConstants.WORLD_WIDTH / 2, 420)
            return
        }
        menuButton.paint(g)
        retryButton.paint(g)
        Assets.gpg_icon_leaderboards?.let { g.drawImage(it, 1175, 5) }
        paint.typeface = Assets.plain
        gameOverBanner.paint(g, paint, "Game Over!", GameConstants.WORLD_WIDTH / 2, 200)
        scoreBanner.paint(g, paint, "Score: ${session.getScore()}",
            GameConstants.WORLD_WIDTH / 2, 400)
        paintGameOverHighlight(g)
    }

    private fun paintGameOverHighlight(g: Graphics) {
        val items = buildGameOverFocusBounds()
        if (gameOverSelection in items.indices) {
            UiSelectionHighlight.paintRect(g, items[gameOverSelection])
        }
    }

    // --- Dual-screen: rear (bottom) display -------------------------------------------------

    private fun isDual(): Boolean = majorProjectGame.isDualScreenActive()

    private fun activePausePanel(): PauseMenuPanel = if (isDual()) rearPausePanel else pauseMenuPanel

    private fun activeQuitDialog(): UiConfirmDialog = if (isDual()) rearQuitDialog else quitConfirmDialog

    override fun onRearTap(x: Int, y: Int) {
        rearTaps.add(x, y)
    }

    override fun paintRear(g: Graphics, paint: Paint) {
        if (disposed) {
            return
        }
        paint.textSize = 40f
        when (state) {
            GameState.Ready -> {
                val focused = PauseMenuPanel.FOCUS_ITEMS.getOrNull(rearReadyFocusIndex)
                rearReadyPanel.paint(g, paint, player, focused, 0f)
            }
            GameState.Running -> paintRearHud(g, paint)
            GameState.Paused -> paintPauseOverlay(g, paint)
            GameState.GameOver -> paintRearGameOver(g)
        }
    }

    private fun paintRearHud(g: Graphics, paint: Paint) {
        val centerX = RearLayout.WIDTH / 2
        paint.textSize = 36f
        UiText.drawCentered(g, paint, "SCORE", centerX, 80, Color.LTGRAY)
        paint.textSize = 110f
        UiText.drawCentered(g, paint, session.getScore().toString(), centerX, 175, Color.WHITE)

        val leftX = RearLayout.WIDTH / 4
        val rightX = RearLayout.WIDTH * 3 / 4
        paint.textSize = 36f
        UiText.drawCentered(g, paint, "BIN", leftX, 320, Color.LTGRAY)
        UiText.drawCentered(g, paint, "FLOOR", rightX, 320, Color.LTGRAY)
        paint.textSize = 84f
        UiText.drawCentered(g, paint, "x${player.getCombo()}", leftX, 400, Color.WHITE)
        UiText.drawCentered(g, paint, "${cleanPercent()}%", rightX, 400, RoombaTheme.DOCK_GLOW)
        rearPauseButton.paint(g)
    }

    /** Just the choices: "Game Over!" and the score are on the top display. */
    private fun paintRearGameOver(g: Graphics) {
        rearRetryButton.paint(g)
        rearMenuButton.paint(g)
        rearLeaderboardButton.paint(g)
        paintGameOverHighlight(g)
    }

    override fun pause() {
        if (disposed) {
            return
        }
        if (state == GameState.Running) {
            openPauseMenu(false)
        }
    }

    override fun resume() {
        if (disposed) {
            return
        }
        if (GamePreferences.music) {
            Assets.playMusic()
            Assets.setMusicVolume(0.25f)
        }
    }

    override fun dispose() {
        disposed = true
    }

    private fun restartRun() {
        markLeaving()
        game.setScreen(GameScreen(majorProjectGame))
    }

    private fun leaveForMenu() {
        markLeaving()
        game.setScreen(MainMenuScreen(majorProjectGame))
        if (GamePreferences.music) {
            Assets.setMusicVolume(0.85f)
        }
    }

    private fun markLeaving() {
        disposed = true
    }

    override fun backButton() {
        if (disposed) {
            return
        }
        when (state) {
            GameState.Running -> openPauseMenu()
            GameState.Paused -> when {
                showQuitConfirm -> {
                    playTap()
                    showQuitConfirm = false
                }
                resumeCountdownSeconds > 0f -> cancelResumeCountdown()
                else -> resumeFromPause()
            }
            GameState.GameOver -> leaveForMenu()
            else -> leaveForMenu()
        }
    }

    private fun changeState(newState: GameState) {
        if (disposed) {
            return
        }
        if (newState == GameState.GameOver) {
            gameOverSelection = GAME_OVER_FOCUS_RETRY
        }
        if (newState == GameState.Paused) {
            pauseFocusIndex = PauseMenuPanel.FOCUS_ITEMS.indexOf(PauseMenuPanel.Item.RESUME)
            resumeCountdownSeconds = 0f
            showQuitConfirm = false
        }
        state = newState
        majorProjectGame.updateSecondaryDisplayForGameState(newState)
    }

    private fun playTap() {
        Assets.tap?.play(GamePreferences.getTapVolume().toFloat())
    }

    fun isGameOverFlag(): Boolean = session.isGameOverFlag()

    fun getFacingAngle(): Float = facingAngle

    fun setFacingAngle(facingAngle: Float) {
        this.facingAngle = facingAngle
    }

    companion object {
        private const val SPAWN_ATTEMPTS = 8
        /** Chance (%) that a spawn tick produces a group formation instead of a single tracker. */
        private const val FORMATION_SPAWN_PERCENT = 25
        private const val READY_FOCUS_START = 0
        private const val READY_FOCUS_SETTINGS_OFFSET = 1

        private const val GAME_OVER_FOCUS_RETRY = 1
        private const val CLEAN_METER_WIDTH = 170

        /** Share of the suction field (beyond the body) that also cleans the floor. */
        private const val CLEAN_SUCTION_SHARE = 0.6f
        private val CLEAN_MILESTONES = floatArrayOf(0.25f, 0.5f, 0.75f, 0.95f)
        private val CLEAN_BONUSES = intArrayOf(250, 500, 1000, 2500)
        private const val REAR_BUTTON_WIDTH = 620
        private const val REAR_DIALOG_Y = 280

        /** Resume button dwell time before it actually resumes the game. */
        private const val RESUME_COUNTDOWN_SECONDS = 3f

        /** Primary action button sits just below the settings panel. */
        val PRIMARY_BUTTON_Y: Int = SettingsPanel.PANEL_Y + SettingsPanel.PANEL_HEIGHT + 12
        /** Prompt sits below the button (button spans PRIMARY_BUTTON_Y..+MENU_HEIGHT). */
        val PROMPT_Y: Int = PRIMARY_BUTTON_Y + UiButton.MENU_HEIGHT + 24
    }
}
