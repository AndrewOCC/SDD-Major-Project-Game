package com.aocc.majorproject.ui;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpatialFocusNavigatorTest {

    @Test
    public void findNext_rightMovesToNearestRightNeighbor() {
        List<UiBounds> items = List.of(
                new UiBounds(100, 100, 50, 50),
                new UiBounds(300, 100, 50, 50),
                new UiBounds(500, 100, 50, 50));
        assertEquals(1, SpatialFocusNavigator.findNext(0,
                SpatialFocusNavigator.Direction.RIGHT, items));
        assertEquals(2, SpatialFocusNavigator.findNext(1,
                SpatialFocusNavigator.Direction.RIGHT, items));
    }

    @Test
    public void findNext_downMovesToItemBelow() {
        List<UiBounds> items = List.of(
                new UiBounds(200, 100, 50, 50),
                new UiBounds(200, 300, 50, 50));
        assertEquals(1, SpatialFocusNavigator.findNext(0,
                SpatialFocusNavigator.Direction.DOWN, items));
    }

    @Test
    public void findNext_ignoresItemsOutsideCone() {
        List<UiBounds> items = List.of(
                new UiBounds(100, 100, 50, 50),
                new UiBounds(100, 400, 50, 50));
        assertEquals(0, SpatialFocusNavigator.findNext(0,
                SpatialFocusNavigator.Direction.RIGHT, items));
    }

    @Test
    public void findNext_leftFromRightItem() {
        List<UiBounds> items = List.of(
                new UiBounds(100, 100, 50, 50),
                new UiBounds(300, 100, 50, 50));
        assertEquals(0, SpatialFocusNavigator.findNext(1,
                SpatialFocusNavigator.Direction.LEFT, items));
    }
}
