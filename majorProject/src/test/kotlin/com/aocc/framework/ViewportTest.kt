package com.aocc.framework

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewportTest {

    @Test
    fun update_letterboxesWideScreenWithUniformScale() {
        val viewport = Viewport()
        viewport.update(2400, 1080)

        assertEquals(1080f / GameConstants.WORLD_HEIGHT, viewport.getScale(), 0.001f)
        assertTrue(viewport.getLetterboxDestRect().left > 0)
    }

    @Test
    fun screenToWorld_mapsCenterOfLetterboxToWorldCenter() {
        val viewport = Viewport()
        viewport.update(1920, 1080)

        val worldX = viewport.screenToWorldX(960f)
        val worldY = viewport.screenToWorldY(540f)

        assertEquals(GameConstants.WORLD_WIDTH / 2f, worldX.toFloat(), 2f)
        assertEquals(GameConstants.WORLD_HEIGHT / 2f, worldY.toFloat(), 2f)
    }

    @Test
    fun offsets_centerLetterboxOnUltrawide() {
        val viewport = Viewport()
        viewport.update(2400, 1080)

        assertTrue(viewport.getOffsetX() > 0)
        assertEquals(0f, viewport.getOffsetY(), 0.001f)
    }
}
