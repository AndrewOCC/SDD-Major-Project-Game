package com.aocc.framework;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ViewportTest {

    @Test
    public void update_letterboxesWideScreenWithUniformScale() {
        Viewport viewport = new Viewport();
        viewport.update(2400, 1080);

        assertEquals(1080f / GameConstants.WORLD_HEIGHT, viewport.getScale(), 0.001f);
        assertTrue(viewport.getLetterboxDestRect().left > 0);
    }

    @Test
    public void screenToWorld_mapsCenterOfLetterboxToWorldCenter() {
        Viewport viewport = new Viewport();
        viewport.update(1920, 1080);

        int worldX = viewport.screenToWorldX(960);
        int worldY = viewport.screenToWorldY(540);

        assertEquals(GameConstants.WORLD_WIDTH / 2, worldX, 2);
        assertEquals(GameConstants.WORLD_HEIGHT / 2, worldY, 2);
    }
}
