package com.aocc.majorproject.display

import org.junit.Assert.assertEquals
import org.junit.Test

class SecondaryDisplayFinderTest {

    @Test
    fun categoryRear_matchesFrameworkCategory() {
        assertEquals(
            "android.hardware.display.category.REAR",
            SecondaryDisplayFinder.CATEGORY_REAR
        )
    }
}
