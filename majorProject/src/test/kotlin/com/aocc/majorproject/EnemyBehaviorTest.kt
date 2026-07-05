package com.aocc.majorproject

import com.aocc.framework.GameConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EnemyBehaviorTest {

    private lateinit var session: GameSession

    @Before
    fun setUp() {
        session = GameSession()
    }

    @Test
    fun driftEnemy_movesAlongTrajectoryWithoutTracking() {
        val controller = EnemyController(session)
        controller.addDrift(100f, 300f, 1, 5f, 0f)
        val enemy = controller.e.first()

        enemy.update(ONE_FRAME)

        // Pure drift: exactly start + velocity, no homing acceleration toward the player.
        assertEquals(105f, enemy.getPosX(), 0.001f)
        assertEquals(300f, enemy.getPosY(), 0.001f)
    }

    @Test
    fun driftEnemy_despawnsOnceFullyOffScreen() {
        val controller = EnemyController(session)
        controller.addDrift((GameConstants.WORLD_WIDTH - 5).toFloat(), 300f, 1, 20f, 0f)

        repeat(60) { controller.update(ONE_FRAME) }

        assertTrue(controller.e.isEmpty())
    }

    @Test
    fun trackingEnemy_staysOnScreen() {
        val controller = EnemyController(session)
        controller.addEnemy(100, 300, 1)

        repeat(120) { controller.update(ONE_FRAME) }

        val enemy = controller.e.firstOrNull()
        if (enemy != null) {
            assertTrue(enemy.getPosX() >= 0f)
            assertTrue(enemy.getPosX() <= GameConstants.WORLD_WIDTH.toFloat())
        }
    }

    @Test
    fun formation_spawnsMultipleDots() {
        val controller = EnemyController(session)
        EnemyFormations(Random(1)).driftRing(controller)

        assertTrue(controller.e.size >= 4)
    }

    companion object {
        private val ONE_FRAME = 1f / GameConstants.REFERENCE_FPS
    }
}
