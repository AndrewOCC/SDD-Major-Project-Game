package com.aocc.majorproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EnemyControllerTest {

    private EnemyController controller;

    @Before
    public void setUp() {
        GameScreen.setSpeed(0);
        GameScreen.setPlayer(new Player());
        controller = new EnemyController();
    }

    @Test
    public void generateNextEnemy_spawnDelayIsWithinExpectedRange() {
        controller.generateNextEnemy(0);

        assertTrue(EnemyController.nextEnemySpawn >= 5);
        assertTrue(EnemyController.nextEnemySpawn <= 40);
    }

    @Test
    public void generateNextEnemy_higherSpeedReducesMaximumDelay() {
        controller.generateNextEnemy(10);

        assertTrue(EnemyController.nextEnemySpawn <= 20 + 5);
    }

    @Test
    public void removeAllEnemies_clearsEnemyList() {
        controller.addEnemy(100, 100, 1);
        controller.addEnemy(200, 200, 2);

        controller.removeAllEnemies();

        assertEquals(0, controller.getE().size());
    }
}
