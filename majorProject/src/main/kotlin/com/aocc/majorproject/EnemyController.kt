package com.aocc.majorproject

import android.graphics.Paint
import com.aocc.framework.Graphics
import java.util.LinkedList
import java.util.Random

/**
 * This controller allows one call in the GameScreen class to update or paint every
 * enemy through a LinkedList; it also allows the easy creation and deletion of enemies
 */
class EnemyController(private val session: GameSession) {

    var e: LinkedList<Enemy> = LinkedList()
    private val r = Random()

    private var nextEnemySpawn = 0

    fun getNextEnemySpawn(): Int = nextEnemySpawn

    fun generateNextEnemy(speed: Int) {
        nextEnemySpawn = r.nextInt(40 - 2 * speed) + MIN_SPAWN_TIME
    }

    fun addEnemy(x: Int, y: Int, t: Int) {
        e.add(Enemy(x.toFloat(), y.toFloat(), t, session))
        generateNextEnemy(session.getSpeed())
    }

    fun removeEnemy(i: Int) {
        e.removeAt(i)
        Assets.zap?.play(GamePreferences.getTapVolume().toFloat())
    }

    fun removeAllEnemies() {
        e.clear()
    }

    fun update(deltaSeconds: Float) {
        var i = 0
        while (i < e.size) {
            val enemy = e[i]
            enemy.update(deltaSeconds)
            if (enemy.getHealth() <= 0) {
                removeEnemy(i)
            } else {
                i++
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
