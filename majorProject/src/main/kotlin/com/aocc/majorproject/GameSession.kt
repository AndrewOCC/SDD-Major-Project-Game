package com.aocc.majorproject

/**
 * Per-playthrough game state. Replaces static fields previously scattered on [GameScreen].
 */
class GameSession {

    private val player = Player(this)
    private var updateCount = 0f
    private var enemyCounter = INITIAL_ENEMY_COUNTER
    private var score = 0
    private var scoreUploaded = false
    private var speed = 0
    private var gameOverFlag = false

    fun getPlayer(): Player = player

    fun getUpdateCount(): Float = updateCount

    fun addUpdateCount(step: Float) {
        updateCount += step
    }

    fun subtractUpdateCount(amount: Float) {
        updateCount -= amount
    }

    fun getEnemyCounter(): Float = enemyCounter

    fun addEnemyCounter(step: Float) {
        enemyCounter += step
    }

    fun resetEnemyCounter() {
        enemyCounter = 0f
    }

    fun getScore(): Int = score

    fun addScore(points: Int) {
        score += points
    }

    fun isScoreUploaded(): Boolean = scoreUploaded

    fun setScoreUploaded(scoreUploaded: Boolean) {
        this.scoreUploaded = scoreUploaded
    }

    fun getSpeed(): Int = speed

    fun incrementSpeed() {
        speed++
    }

    /** Debug-popup override for the speed ramp (0..[com.aocc.framework.GameConstants.SPEED_RAMP_MAX]). */
    fun setSpeed(speed: Int) {
        this.speed = speed
    }

    fun isGameOverFlag(): Boolean = gameOverFlag

    fun setGameOverFlag(gameOverFlag: Boolean) {
        this.gameOverFlag = gameOverFlag
    }

    /** Resets counters for a new run while keeping the same screen instance. */
    fun resetForNewRun() {
        updateCount = 0f
        enemyCounter = INITIAL_ENEMY_COUNTER
        score = 0
        scoreUploaded = false
        speed = 0
        gameOverFlag = false
    }

    companion object {
        private const val INITIAL_ENEMY_COUNTER = 300f
    }
}
