package com.aocc.majorproject.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MainMenuFocusBoundsTest {

    @Test
    fun signedOutMenu_hasFiveFocusableItems() {
        assertEquals(5, MainMenuLayout.menuItemCount(false))
        for (i in 0 until 5) {
            assertNotNull(
                "signed-out menu item $i must have highlight bounds",
                MainMenuLayout.highlightForIndex(i, false)
            )
        }
        assertNull(MainMenuLayout.highlightForIndex(5, false))
    }

    @Test
    fun signedInMenu_hasFourFocusableItems() {
        assertEquals(4, MainMenuLayout.menuItemCount(true))
        for (i in 0 until 4) {
            assertNotNull(
                "signed-in menu item $i must have highlight bounds",
                MainMenuLayout.highlightForIndex(i, true)
            )
        }
        assertNull(MainMenuLayout.highlightForIndex(4, true))
    }

    @Test
    fun signedOutMenu_includesSignInBounds() {
        assertEquals(MainMenuLayout.signInButton(), MainMenuLayout.highlightForIndex(2, false))
    }
}
