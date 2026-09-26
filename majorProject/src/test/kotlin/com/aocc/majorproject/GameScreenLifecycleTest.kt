package com.aocc.majorproject

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GameScreenLifecycleTest {

    @Test
    fun dispose_keepsStateWithoutNullingReferences() {
        val activity = Robolectric.buildActivity(MajorProjectGame::class.java).create().get()
        val screen = GameScreen(activity)
        assertEquals(GameScreen.GameState.Ready, screen.state)

        screen.dispose()

        assertEquals(GameScreen.GameState.Ready, screen.state)
    }
}
