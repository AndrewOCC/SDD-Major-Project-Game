package com.aocc.majorproject

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GameSessionTest {

    private lateinit var session: GameSession

    @Before
    fun setUp() {
        session = GameSession()
    }

    @Test
    fun newSession_startsWithZeroScoreAndNoGameOver() {
        assertEquals(0, session.getScore())
        assertFalse(session.isGameOverFlag())
        assertEquals(0, session.getSpeed())
    }

    @Test
    fun addScore_accumulatesPoints() {
        session.addScore(100)
        session.addScore(50)
        assertEquals(150, session.getScore())
    }

    @Test
    fun resetForNewRun_clearsProgressFlags() {
        session.addScore(500)
        session.setGameOverFlag(true)
        session.setScoreUploaded(true)
        session.incrementSpeed()

        session.resetForNewRun()

        assertEquals(0, session.getScore())
        assertFalse(session.isGameOverFlag())
        assertFalse(session.isScoreUploaded())
        assertEquals(0, session.getSpeed())
    }

    @Test
    fun player_isBoundToSession() {
        assertNotNull(session.getPlayer())
    }
}
