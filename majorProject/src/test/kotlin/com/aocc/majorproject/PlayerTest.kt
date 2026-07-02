package com.aocc.majorproject

import com.aocc.framework.GameConstants
import com.aocc.framework.implementation.RotationHandler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PlayerTest {

    private lateinit var session: GameSession
    private lateinit var player: Player

    @Before
    fun setUp() {
        session = GameSession()
        setRotation(0f, 0f)
        player = session.getPlayer()
    }

    @Test
    fun update_clampsPlayerInsideLeftBoundary() {
        player.setDefaultX(0)
        player.setDefaultY(320)
        setRotation(-90f, 0f)

        player.update(ONE_FRAME)

        assertEquals(3f, player.getDefaultX(), 0.001f)
        assertEquals(0f, player.getVelocityX(), 0.001f)
    }

    @Test
    fun update_clampsPlayerInsideRightBoundary() {
        player.setDefaultX(1220)
        player.setDefaultY(320)
        setRotation(90f, 0f)

        player.update(ONE_FRAME)

        assertTrue(player.getDefaultX() < 1280)
        assertEquals(0f, player.getVelocityX(), 0.001f)
    }

    @Test
    fun update_decrementsShieldEachFrame() {
        player.setShield(100)

        player.update(ONE_FRAME)

        assertEquals(99, player.getShield())
    }

    @Test
    fun update_setsGameOverWhenHealthReachesZero() {
        player.setHealth(0)

        player.update(ONE_FRAME)

        assertTrue(session.isGameOverFlag())
    }

    @Test
    fun update_clampsHealthToMaximumValue() {
        player.setHealth(10)

        player.update(ONE_FRAME)

        assertEquals(5, player.getHealth())
    }

    private fun setRotation(x: Float, y: Float) {
        RotationHandler.screenX = x
        RotationHandler.screenY = y
    }

    companion object {
        private val ONE_FRAME = 1f / GameConstants.REFERENCE_FPS
    }
}
