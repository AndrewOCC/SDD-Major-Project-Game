package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.Input.TouchEvent
import com.aocc.framework.PersonalMethods
import com.aocc.framework.Screen
import com.aocc.majorproject.input.GamepadInput
import com.aocc.majorproject.ui.ComboMeter
import com.aocc.majorproject.ui.ScoreBar
import com.aocc.majorproject.ui.SettingsPanel
import com.aocc.majorproject.ui.SpatialFocusNavigator
import com.aocc.majorproject.ui.UiBanner
import com.aocc.majorproject.ui.UiButton
import com.aocc.majorproject.ui.UiBounds
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
    private val powerUp = PowerUp(1, session)
    private val tempEnemyPoint = Point()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private lateinit var menuButton: UiButton
    private lateinit var resumeButton: UiButton
    private lateinit var retryButton: UiButton
    private val settingsPanel = SettingsPanel()

    private val scoreBar = ScoreBar()
    private val comboMeter = ComboMeter()
    private val promptBanner = UiBanner(50f)
    private val gameOverBanner = UiBanner(100f)
    private val scoreBanner = UiBanner(60f)

    private var facingAngle = 0f
    private var pauseFocusIndex = 0
    private var gameOverSelection = 0

    init {
        settingsPanel.setGame(majorProjectGame)
        GamePreferences.applyTiltTo(player)

        menuButton = UiButton.menuAt(0, 0)
        resumeButton = UiButton(
            UiLayout.centerX(UiButton.MENU_WIDTH), 605,
            UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT, "Resume"
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
            GameState.Running -> updateRunning(touchEvents, deltaTime)
            GameState.Paused -> {
                updatePaused(touchEvents)
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
                if (settingsPanel.handleTouch(event, player)) {
                    continue
                }
                playTap()
                changeState(GameState.Running)
            }
        }
    }

    private fun handleGamepadReady(actions: List<GamepadInput.Action>) {
        for (action in actions) {
            if (action == GamepadInput.Action.CANCEL) {
                leaveForMenu()
                return
            }
            if (settingsPanel.handleGamepad(action, player)) {
                continue
            }
            if (action == GamepadInput.Action.CONFIRM) {
                playTap()
                changeState(GameState.Running)
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

        if (session.getSpeed() < 25 && session.getUpdateCount() >= GameConstants.SPEED_RAMP_INTERVAL_FRAMES) {
            session.incrementSpeed()
            session.subtractUpdateCount(GameConstants.SPEED_RAMP_INTERVAL_FRAMES)
            enemyController.increaseEnemyTopSpeed()
        }

        if (session.getEnemyCounter() > enemyController.getNextEnemySpawn()) {
            tempEnemyPoint.x = random.nextInt(GameConstants.WORLD_WIDTH - 100) + 50
            tempEnemyPoint.y = random.nextInt(GameConstants.WORLD_HEIGHT - 100) + 50
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
            session.resetEnemyCounter()
        }

        if (player.getCombo() == 200) {
            majorProjectGame.onAchievementUnlocked(
                majorProjectGame.getString(R.string.achievement_ccccombo)
            )
        }

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

    private fun updatePaused(touchEvents: List<TouchEvent>) {
        for (event in touchEvents) {
            if (event.type == TouchEvent.TOUCH_UP) {
                if (resumeButton.touchInBounds(event)) {
                    resumeFromPause()
                    return
                }
                if (menuButton.touchInBounds(event)) {
                    playTap()
                    leaveForMenu()
                    return
                }
                if (settingsPanel.handleTouch(event, player)) {
                    continue
                }
                resumeFromPause()
            }
        }
    }

    private fun handleGamepadPaused(actions: List<GamepadInput.Action>) {
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
            if (action == GamepadInput.Action.CANCEL) {
                resumeFromPause()
            }
        }
    }

    private fun activatePauseFocus() {
        when (pauseFocusIndex) {
            PAUSE_FOCUS_RESUME -> resumeFromPause()
            PAUSE_FOCUS_MENU -> {
                playTap()
                leaveForMenu()
            }
            else -> settingsPanel.activateFocusIndex(
                pauseFocusIndex - PAUSE_FOCUS_SETTINGS_OFFSET, player
            )
        }
    }

    private fun buildPauseFocusBounds(): List<UiBounds> {
        val items = ArrayList<UiBounds>(PAUSE_FOCUS_SETTINGS_OFFSET + SettingsPanel.ITEM_COUNT)
        items.add(resumeButton.getBounds())
        items.add(menuButton.getBounds())
        for (i in 0 until SettingsPanel.ITEM_COUNT) {
            settingsPanel.getItemBounds(i)?.let { items.add(it) }
        }
        return items
    }

    private fun resumeFromPause() {
        playTap()
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
        settingsPanel.paint(g, paint, player)
        paint.typeface = Assets.plain
        promptBanner.paint(g, paint, "Press anywhere to start",
            GameConstants.WORLD_WIDTH / 2, PROMPT_Y)
    }

    private fun drawRunningUI(g: Graphics) {
        powerUp.paint(g, paint)
        enemyController.paint(g, paint)
        player.paint(g, paint)
        paint.typeface = Assets.plain
        comboMeter.paint(g, paint, player.getCombo())
        scoreBar.paint(g, paint, session.getScore())
        menuButton.paint(g)
    }

    private fun drawPausedUI(g: Graphics) {
        g.drawRect(0, 0, 1281, 721, Color.BLACK)
        paint.textSize = 40f
        powerUp.paint(g, paint)
        enemyController.paint(g, paint)
        player.paint(g, paint)
        g.drawARGB(155, 0, 0, 0)
        val settingsHighlight = if (pauseFocusIndex >= PAUSE_FOCUS_SETTINGS_OFFSET) {
            pauseFocusIndex - PAUSE_FOCUS_SETTINGS_OFFSET
        } else {
            -1
        }
        settingsPanel.paint(g, paint, player, settingsHighlight)
        resumeButton.paint(g)
        menuButton.paint(g)
        paintPauseFocusHighlight(g)
        paint.typeface = Assets.plain
        promptBanner.paint(g, paint, "Press Resume or anywhere to continue",
            GameConstants.WORLD_WIDTH / 2, PROMPT_Y)
    }

    private fun paintPauseFocusHighlight(g: Graphics) {
        when (pauseFocusIndex) {
            PAUSE_FOCUS_RESUME -> UiSelectionHighlight.paintRect(g, resumeButton.getBounds())
            PAUSE_FOCUS_MENU -> UiSelectionHighlight.paintRect(g, menuButton.getBounds())
        }
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
            GameState.Paused -> resumeFromPause()
            GameState.GameOver -> leaveForMenu()
            else -> leaveForMenu()
        }
    }

    private fun changeState(newState: GameState) {
        if (disposed) {
            return
        }
        if (newState == GameState.Paused) {
            pauseFocusIndex = PAUSE_FOCUS_RESUME
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
        private const val PAUSE_FOCUS_RESUME = 0
        private const val PAUSE_FOCUS_MENU = 1
        private const val PAUSE_FOCUS_SETTINGS_OFFSET = 2
        val PROMPT_Y: Int = SettingsPanel.PANEL_Y + SettingsPanel.PANEL_HEIGHT + 65
    }
}
