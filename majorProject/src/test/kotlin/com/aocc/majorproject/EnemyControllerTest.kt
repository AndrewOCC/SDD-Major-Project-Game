package com.aocc.majorproject

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EnemyControllerTest {

    private lateinit var session: GameSession
    private lateinit var controller: EnemyController

    @Before
    fun setUp() {
        session = GameSession()
        controller = EnemyController(session)
    }

    @Test
    fun generateNextEnemy_spawnDelayIsWithinExpectedRange() {
        controller.generateNextEnemy(0)

        assertTrue(controller.getNextEnemySpawn() >= 5)
        assertTrue(controller.getNextEnemySpawn() <= 44)
    }

    @Test
    fun generateNextEnemy_higherSpeedReducesMaximumDelay() {
        controller.generateNextEnemy(10)

        assertTrue(controller.getNextEnemySpawn() <= 20 + 5)
    }

    @Test
    fun removeAllEnemies_clearsEnemyList() {
        controller.addEnemy(100, 100, 1)
        controller.addEnemy(200, 200, 2)

        controller.removeAllEnemies()

        assertEquals(0, controller.e.size)
    }
}
