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
import com.aocc.majorproject.display.SecondaryPauseState
import com.aocc.majorproject.input.GamepadInput
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
import java.util.Random

class GameScreen(val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame) {

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

    private val scoreBar = ScoreBar()
    private val comboMeter = ComboMeter()
    private val promptBanner = UiBanner(34f)
    private val gameOverBanner = UiBanner(100f)
    private val scoreBanner = UiBanner(60f)

    private var facingAngle = 0f
    private var readyFocusIndex = READY_FOCUS_START
    private var pauseFocusIndex = 0
    private var resumeCountdownSeconds = 0f
    private var showQuitConfirm = false
    private var quitConfirmFocusIndex = 0
    private var gameOverSelection = 0

    init {
        settingsPanel.setGame(majorProjectGame)
        pauseMenuPanel.setGame(majorProjectGame)
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

        val touchEvents = game.input.touchEvents
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
        for (event in touchEvents) {
            if (event.type == TouchEvent.TOUCH_UP && menuButton.touchInBounds(event)) {
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
                val spawnTop = GameConstants.PLAY_AREA_TOP + 20
                tempEnemyPoint.x = random.nextInt(GameConstants.WORLD_WIDTH - 100) + 50
                tempEnemyPoint.y = random.nextInt(GameConstants.WORLD_HEIGHT - 50 - spawnTop) + spawnTop
                PersonalMethods.limitOutside(
                    tempEnemyPoint,
                    player.getCenterX().toInt(),
                    player.getCenterY().toInt(),
                    100
                )
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

        majorProjectGame.updateSecondaryDisplayStats(session.getScore(), player.getCombo())
        majorProjectGame.updateSecondaryDebugState(
            SecondaryDebugState(player.debugInvincible, session.getSpeed())
        )

        if (session.isGameOverFlag()) {
            changeState(GameState.GameOver)
        }
    }

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
        majorProjectGame.updateSecondaryPauseState(
            SecondaryPauseState(
                GamePreferences.sound, GamePreferences.music, GamePreferences.secondScreenEnabled,
                player.tiltMode, resumeCountdownSeconds, showQuitConfirm
            )
        )
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
                if (pauseMenuPanel.getResumeBounds().contains(event)) {
                    activateSecondaryPauseItem(PauseMenuPanel.Item.RESUME)
                    return
                }
                if (pauseMenuPanel.getQuitBounds().contains(event)) {
                    activateSecondaryPauseItem(PauseMenuPanel.Item.QUIT)
                    return
                }
                pauseMenuPanel.handleSettingsTouch(event, player)
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
            if (event.type == TouchEvent.TOUCH_UP && pauseMenuPanel.getResumeBounds().contains(event)) {
                activateSecondaryPauseItem(PauseMenuPanel.Item.RESUME)
                return
            }
        }
    }

    private fun handleQuitConfirmTouch(touchEvents: List<TouchEvent>) {
        for (event in touchEvents) {
            if (event.type != TouchEvent.TOUCH_UP) {
                continue
            }
            if (quitConfirmDialog.getConfirmBounds().contains(event)) {
                confirmSecondaryQuit(true)
                return
            }
            if (quitConfirmDialog.getCancelBounds().contains(event)) {
                confirmSecondaryQuit(false)
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
                confirmSecondaryQuit(quitConfirmFocusIndex == 0)
                continue
            }
            if (action == GamepadInput.Action.CANCEL || action == GamepadInput.Action.PAUSE) {
                confirmSecondaryQuit(false)
            }
        }
    }

    private fun activatePauseFocus() {
        activateSecondaryPauseItem(PauseMenuPanel.FOCUS_ITEMS[pauseFocusIndex])
    }

    /**
     * Single entry point for activating a pause-menu item, shared by touch, gamepad, and the
     * native controls mirrored onto a secondary (rear) display.
     */
    fun activateSecondaryPauseItem(item: PauseMenuPanel.Item) {
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

    /** Invoked by the "are you sure?" confirmation, whether on the primary or rear display. */
    fun confirmSecondaryQuit(confirmed: Boolean) {
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
        return PauseMenuPanel.FOCUS_ITEMS.map { pauseMenuPanel.getItemBounds(it) }
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

    private fun buildGameOverFocusBounds(): List<UiBounds> {
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
        val background = Assets.game_bg
        if (background != null) {
            g.drawImage(background, 0, 0)
        } else {
            g.drawRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, Color.BLACK)
        }

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
        val settingsHighlight = if (readyFocusIndex >= READY_FOCUS_SETTINGS_OFFSET) {
            readyFocusIndex - READY_FOCUS_SETTINGS_OFFSET
        } else {
            -1
        }
        settingsPanel.paint(g, paint, player, settingsHighlight)
        startButton.paint(g)
        if (readyFocusIndex == READY_FOCUS_START) {
            UiSelectionHighlight.paintRect(g, startButton.getBounds())
        }
        paint.typeface = Assets.plain
        promptBanner.paint(g, paint, "Press Start or tap anywhere to begin",
            GameConstants.WORLD_WIDTH / 2, PROMPT_Y)
    }

    private fun drawRunningUI(g: Graphics) {
        powerUp.paint(g, paint)
        enemyController.paint(g, paint)
        player.paint(g, paint)
        paint.typeface = Assets.plain
        // Score/combo move to the rear display while it's actively mirroring gameplay.
        if (!majorProjectGame.isSecondaryDisplayPresentingStats()) {
            comboMeter.paint(g, paint, player.getCombo())
            scoreBar.paint(g, paint, session.getScore())
        }
        menuButton.paint(g)
    }

    private fun drawPausedUI(g: Graphics) {
        g.drawRect(0, 0, 1281, 721, Color.BLACK)
        paint.textSize = 40f
        powerUp.paint(g, paint)
        enemyController.paint(g, paint)
        player.paint(g, paint)
        g.drawARGB(155, 0, 0, 0)
        paint.typeface = Assets.plain

        if (showQuitConfirm) {
            quitConfirmDialog.paint(g, paint)
            paintQuitConfirmHighlight(g)
            return
        }

        val focusedItem = PauseMenuPanel.FOCUS_ITEMS.getOrNull(pauseFocusIndex)
        pauseMenuPanel.paint(g, paint, player, focusedItem, resumeCountdownSeconds)
    }

    private fun paintQuitConfirmHighlight(g: Graphics) {
        val bounds = if (quitConfirmFocusIndex == 0) {
            quitConfirmDialog.getConfirmBounds()
        } else {
            quitConfirmDialog.getCancelBounds()
        }
        UiSelectionHighlight.paintRect(g, bounds)
    }

    private fun drawGameOverUI(g: Graphics) {
        g.drawARGB(155, 0, 0, 0)
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
        /** Chance (%) that a spawn tick produces a group formation instead of a single tracker. */
        private const val FORMATION_SPAWN_PERCENT = 25
        private const val READY_FOCUS_START = 0
        private const val READY_FOCUS_SETTINGS_OFFSET = 1

        /** Resume button dwell time before it actually resumes the game. */
        private const val RESUME_COUNTDOWN_SECONDS = 3f

        /** Primary action button sits just below the settings panel. */
        val PRIMARY_BUTTON_Y: Int = SettingsPanel.PANEL_Y + SettingsPanel.PANEL_HEIGHT + 12
        /** Prompt sits below the button (button spans PRIMARY_BUTTON_Y..+MENU_HEIGHT). */
        val PROMPT_Y: Int = PRIMARY_BUTTON_Y + UiButton.MENU_HEIGHT + 24
    }
}
