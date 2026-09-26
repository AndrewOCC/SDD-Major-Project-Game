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
    fun enemy_staysAtSpawnPointWhileSpawningIn() {
        val controller = EnemyController(session)
        controller.addDrift(100f, 300f, 1, 5f, 0f)
        val enemy = controller.e.first()

        // Well within the spawn-in window (1.0s-1.8s depending on session speed).
        repeat(30) { enemy.update(ONE_FRAME) } // 0.5s

        assertEquals(100f, enemy.getPosX(), 0.001f)
        assertEquals(300f, enemy.getPosY(), 0.001f)
    }

    @Test
    fun driftEnemy_movesAlongTrajectoryWithoutTracking() {
        val controller = EnemyController(session)
        controller.addDrift(100f, 300f, 1, 5f, 0f)
        val enemy = controller.e.first()

        // Run past the (session-speed-dependent) spawn-in window so movement has started.
        repeat(SPAWN_WAIT_FRAMES) { enemy.update(ONE_FRAME) }
        val xBeforeStep = enemy.getPosX()

        enemy.update(ONE_FRAME)

        // Pure drift: exactly + velocity, no homing acceleration toward the player.
        assertEquals(xBeforeStep + 5f, enemy.getPosX(), 0.001f)
        assertEquals(300f, enemy.getPosY(), 0.001f)
    }

    @Test
    fun driftEnemy_despawnsOnceFullyOffScreen() {
        val controller = EnemyController(session)
        controller.addDrift((GameConstants.WORLD_WIDTH - 5).toFloat(), 300f, 1, 20f, 0f)

        repeat(SPAWN_WAIT_FRAMES + 10) { controller.update(ONE_FRAME) }

        assertTrue(controller.e.isEmpty())
    }

    @Test
    fun spawnSecondsFor_shortensAsSessionSpeedIncreases() {
        assertEquals(1.8f, Enemy.spawnSecondsFor(0), 0.001f)
        assertEquals(1.0f, Enemy.spawnSecondsFor(GameConstants.SPEED_RAMP_MAX), 0.001f)
        assertTrue(Enemy.spawnSecondsFor(12) < Enemy.spawnSecondsFor(0))
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

    @Test
    fun fullLine_spansFromNearTopToBottomOfPlayArea() {
        val controller = EnemyController(session)
        EnemyFormations(Random(2)).fullLine(controller)

        val minY = controller.e.minOf { it.getPosY() }
        val maxY = controller.e.maxOf { it.getPosY() }
        assertTrue(minY <= GameConstants.PLAY_AREA_TOP + 80f)
        assertTrue(maxY >= GameConstants.WORLD_HEIGHT - 120f)
    }

    @Test
    fun box_spawnsColsTimesRows() {
        val controller = EnemyController(session)
        EnemyFormations(Random(3)).box(controller, cols = 4, rows = 3)

        assertEquals(12, controller.e.size)
    }

    @Test
    fun circleAroundPlayer_surroundsPlayerWithTrackers() {
        session.getPlayer().setDefaultX(600f)
        session.getPlayer().setDefaultY(320f)
        session.getPlayer().update(1f / GameConstants.REFERENCE_FPS)
        val controller = EnemyController(session)

        EnemyFormations(Random(4)).circleAroundPlayer(controller, session.getPlayer(), count = 8)

        assertEquals(8, controller.e.size)
    }

    @Test
    fun aimingArrow_aimsAtPlayerWhileFormingThenLaunches() {
        val player = session.getPlayer()
        player.setDefaultX(600f)
        player.setDefaultY(600f)
        player.update(ONE_FRAME)
        val controller = EnemyController(session)

        EnemyFormations(Random(5)).aimingArrow(controller, player)
        assertEquals(6, controller.e.size)

        // One forming frame positions the arrow aiming down toward the player.
        controller.update(ONE_FRAME)
        val formedMaxY = controller.e.maxOf { it.getPosY() }
        assertTrue(formedMaxY > GameConstants.PLAY_AREA_TOP.toFloat())

        // After the forming window (matches the dots' own spawn-in duration) it launches
        // toward the player (downward here).
        repeat(SPAWN_WAIT_FRAMES + 10) { controller.update(ONE_FRAME) }
        val launched = controller.e.isEmpty() ||
            controller.e.maxOf { it.getPosY() } > formedMaxY + 20f
        assertTrue(launched)
    }

    companion object {
        private val ONE_FRAME = 1f / GameConstants.REFERENCE_FPS
        /** Frames to fast-forward past the longest possible spawn-in window (1.8s at 60fps). */
        private const val SPAWN_WAIT_FRAMES = 110
    }
}
