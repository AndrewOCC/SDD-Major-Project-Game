package com.aocc.majorproject.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SpatialFocusNavigatorTest {

    @Test
    fun findNext_rightMovesToNearestRightNeighbor() {
        val items = listOf(
            UiBounds(100, 100, 50, 50),
            UiBounds(300, 100, 50, 50),
            UiBounds(500, 100, 50, 50)
        )
        assertEquals(
            1,
            SpatialFocusNavigator.findNext(0, SpatialFocusNavigator.Direction.RIGHT, items)
        )
        assertEquals(
            2,
            SpatialFocusNavigator.findNext(1, SpatialFocusNavigator.Direction.RIGHT, items)
        )
    }

    @Test
    fun findNext_downMovesToItemBelow() {
        val items = listOf(
            UiBounds(200, 100, 50, 50),
            UiBounds(200, 300, 50, 50)
        )
        assertEquals(
            1,
            SpatialFocusNavigator.findNext(0, SpatialFocusNavigator.Direction.DOWN, items)
        )
    }

    @Test
    fun findNext_ignoresItemsOutsideCone() {
        val items = listOf(
            UiBounds(100, 100, 50, 50),
            UiBounds(100, 400, 50, 50)
        )
        assertEquals(
            0,
            SpatialFocusNavigator.findNext(0, SpatialFocusNavigator.Direction.RIGHT, items)
        )
    }

    @Test
    fun findNext_leftFromRightItem() {
        val items = listOf(
            UiBounds(100, 100, 50, 50),
            UiBounds(300, 100, 50, 50)
        )
        assertEquals(
            0,
            SpatialFocusNavigator.findNext(1, SpatialFocusNavigator.Direction.LEFT, items)
        )
    }
}
