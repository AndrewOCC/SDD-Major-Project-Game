package com.aocc.majorproject;

/**
 * Per-playthrough game state. Replaces static fields previously scattered on {@link GameScreen}.
 */
public class GameSession {

    private static final float INITIAL_ENEMY_COUNTER = 300f;

    private final Player player;
    private float updateCount;
    private float enemyCounter = INITIAL_ENEMY_COUNTER;
    private int score;
    private boolean scoreUploaded;
    private int speed;
    private boolean gameOverFlag;

    public GameSession() {
        player = new Player(this);
    }

    public Player getPlayer() {
        return player;
    }

    public float getUpdateCount() {
        return updateCount;
    }

    public void addUpdateCount(float step) {
        updateCount += step;
    }

    public void subtractUpdateCount(float amount) {
        updateCount -= amount;
    }

    public float getEnemyCounter() {
        return enemyCounter;
    }

    public void addEnemyCounter(float step) {
        enemyCounter += step;
    }

    public void resetEnemyCounter() {
        enemyCounter = 0;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }

    public boolean isScoreUploaded() {
        return scoreUploaded;
    }

    public void setScoreUploaded(boolean scoreUploaded) {
        this.scoreUploaded = scoreUploaded;
    }

    public int getSpeed() {
        return speed;
    }

    public void incrementSpeed() {
        speed++;
    }

    public boolean isGameOverFlag() {
        return gameOverFlag;
    }

    public void setGameOverFlag(boolean gameOverFlag) {
        this.gameOverFlag = gameOverFlag;
    }

    /** Resets counters for a new run while keeping the same screen instance. */
    public void resetForNewRun() {
        updateCount = 0;
        enemyCounter = INITIAL_ENEMY_COUNTER;
        score = 0;
        scoreUploaded = false;
        speed = 0;
        gameOverFlag = false;
    }
}
