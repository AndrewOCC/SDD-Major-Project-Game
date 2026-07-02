package com.aocc.majorproject.display;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SecondaryDisplayFinderTest {

    @Test
    public void categoryRear_matchesFrameworkCategory() {
        assertEquals("android.hardware.display.category.REAR",
                SecondaryDisplayFinder.CATEGORY_REAR);
    }
}
