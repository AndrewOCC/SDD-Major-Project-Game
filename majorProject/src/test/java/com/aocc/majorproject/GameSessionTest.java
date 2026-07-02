package com.aocc.majorproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameSessionTest {

    private GameSession session;

    @Before
    public void setUp() {
        session = new GameSession();
    }

    @Test
    public void newSession_startsWithZeroScoreAndNoGameOver() {
        assertEquals(0, session.getScore());
        assertFalse(session.isGameOverFlag());
        assertEquals(0, session.getSpeed());
    }

    @Test
    public void addScore_accumulatesPoints() {
        session.addScore(100);
        session.addScore(50);
        assertEquals(150, session.getScore());
    }

    @Test
    public void resetForNewRun_clearsProgressFlags() {
        session.addScore(500);
        session.setGameOverFlag(true);
        session.setScoreUploaded(true);
        session.incrementSpeed();

        session.resetForNewRun();

        assertEquals(0, session.getScore());
        assertFalse(session.isGameOverFlag());
        assertFalse(session.isScoreUploaded());
        assertEquals(0, session.getSpeed());
    }

    @Test
    public void player_isBoundToSession() {
        assertTrue(session.getPlayer() != null);
    }
}
