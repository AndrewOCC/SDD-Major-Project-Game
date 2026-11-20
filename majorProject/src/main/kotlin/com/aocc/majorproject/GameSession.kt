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

    /** False once the owner is home: mites flee and can no longer hurt or be vacuumed. */
    var contactsEnabled = true

    /** Set when the run ended by surviving until the owner got home (not by running flat). */
    var levelComplete = false

    /** A mite vacuumed up at ([x], [y]) worth [points]; drained each frame by the screen for effects. */
    class Vacuumed(val x: Float, val y: Float, val points: Int)

    private val vacuumed = ArrayList<Vacuumed>()

    fun recordVacuumed(x: Float, y: Float, points: Int) {
        vacuumed.add(Vacuumed(x, y, points))
    }

    fun drainVacuumed(): List<Vacuumed> {
        if (vacuumed.isEmpty()) {
            return emptyList()
        }
        val drained = ArrayList(vacuumed)
        vacuumed.clear()
        return drained
    }

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
        contactsEnabled = true
        levelComplete = false
    }

    companion object {
        private const val INITIAL_ENEMY_COUNTER = 300f
    }
}
