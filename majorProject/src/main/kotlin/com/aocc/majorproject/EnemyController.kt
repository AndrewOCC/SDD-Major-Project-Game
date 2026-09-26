package com.aocc.majorproject

import android.graphics.Paint
import android.graphics.PointF
import com.aocc.framework.Graphics
import java.util.LinkedList
import java.util.Random

/**
 * This controller allows one call in the GameScreen class to update or paint every
 * enemy through a LinkedList; it also allows the easy creation and deletion of enemies
 */
class EnemyController(private val session: GameSession) {

    var e: LinkedList<Enemy> = LinkedList()
    private val activeFormations = LinkedList<ActiveFormation>()
    private val r = Random()

    private var nextEnemySpawn = 0

    fun getNextEnemySpawn(): Int = nextEnemySpawn

    fun generateNextEnemy(speed: Int) {
        nextEnemySpawn = r.nextInt(40 - 2 * speed) + MIN_SPAWN_TIME
    }

    /** Default homing dot that tracks the player. */
    fun addEnemy(x: Int, y: Int, t: Int) {
        e.add(Enemy(x.toFloat(), y.toFloat(), t, session))
        generateNextEnemy(session.getSpeed())
    }

    /** Homing dot added without advancing the single-spawn cadence (for formations). */
    fun addTracking(x: Float, y: Float, t: Int) {
        e.add(Enemy(x, y, t, session, Enemy.Movement.TRACK))
    }

    /** Straight-line dot with no tracking; drifts across the screen and despawns off-edge. */
    fun addDrift(x: Float, y: Float, t: Int, vx: Float, vy: Float) {
        e.add(Enemy(x, y, t, session, Enemy.Movement.DRIFT, driftVx = vx, driftVy = vy))
    }

    /** Dot that follows a prescribed shape / trajectory, then drifts off and despawns. */
    fun addPath(x: Float, y: Float, t: Int, waypoints: List<PointF>, speed: Float) {
        e.add(Enemy(x, y, t, session, Enemy.Movement.PATH, path = waypoints, pathSpeed = speed))
    }

    /** Held dot whose position is driven by its formation until launched. Returns it. */
    fun addHeld(x: Float, y: Float, t: Int): Enemy {
        val enemy = Enemy(x, y, t, session, Enemy.Movement.HELD)
        e.add(enemy)
        return enemy
    }

    fun addFormation(formation: ActiveFormation) {
        activeFormations.add(formation)
    }

    fun removeEnemy(i: Int) {
        e.removeAt(i)
        Assets.zap?.play(GamePreferences.getTapVolume().toFloat())
    }

    fun removeAllEnemies() {
        e.clear()
        activeFormations.clear()
    }

    fun update(deltaSeconds: Float) {
        val formationIterator = activeFormations.iterator()
        while (formationIterator.hasNext()) {
            val formation = formationIterator.next()
            formation.update(deltaSeconds)
            if (formation.isComplete()) {
                formationIterator.remove()
            }
        }

        var i = 0
        while (i < e.size) {
            val enemy = e[i]
            enemy.update(deltaSeconds)
            when {
                enemy.getHealth() <= 0 -> removeEnemy(i)
                enemy.isDespawned() -> e.removeAt(i)
                else -> i++
            }
        }
    }

    fun increaseEnemyTopSpeed() {
        for (enemy in e) {
            enemy.increaseTopSpeed()
        }
    }

    fun paint(g: Graphics, paint: Paint) {
        for (enemy in e) {
            enemy.paint(g, paint)
        }
    }

    companion object {
        private const val MIN_SPAWN_TIME = 5
    }
}
