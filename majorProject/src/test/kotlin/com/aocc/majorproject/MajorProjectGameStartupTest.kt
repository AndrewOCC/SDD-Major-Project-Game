package com.aocc.majorproject

import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MajorProjectGameStartupTest {

    @Test
    fun onCreate_doesNotCrash() {
        val controller = Robolectric.buildActivity(MajorProjectGame::class.java)
        val activity = controller.create().start().resume().get()
        assertNotNull(activity.currentScreen)
        controller.pause().stop().destroy()
    }
}
