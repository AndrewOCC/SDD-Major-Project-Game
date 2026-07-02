package com.aocc.majorproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EnemyControllerTest {

    private GameSession session;
    private EnemyController controller;

    @Before
    public void setUp() {
        session = new GameSession();
        controller = new EnemyController(session);
    }

    @Test
    public void generateNextEnemy_spawnDelayIsWithinExpectedRange() {
        controller.generateNextEnemy(0);

        assertTrue(controller.getNextEnemySpawn() >= 5);
        assertTrue(controller.getNextEnemySpawn() <= 44);
    }

    @Test
    public void generateNextEnemy_higherSpeedReducesMaximumDelay() {
        controller.generateNextEnemy(10);

        assertTrue(controller.getNextEnemySpawn() <= 20 + 5);
    }

    @Test
    public void removeAllEnemies_clearsEnemyList() {
        controller.addEnemy(100, 100, 1);
        controller.addEnemy(200, 200, 2);

        controller.removeAllEnemies();

        assertEquals(0, controller.getE().size());
    }
}
