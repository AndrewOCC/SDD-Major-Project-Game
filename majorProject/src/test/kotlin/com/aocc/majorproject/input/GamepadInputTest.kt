package com.aocc.majorproject.input

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GamepadInputTest {

    @Test
    fun actionForKeyCode_mapsDpadAndButtons() {
        assertEquals(GamepadInput.Action.UP, GamepadInput.actionForKeyCode(KeyEvent.KEYCODE_DPAD_UP))
        assertEquals(
            GamepadInput.Action.DOWN,
            GamepadInput.actionForKeyCode(KeyEvent.KEYCODE_DPAD_DOWN)
        )
        assertEquals(
            GamepadInput.Action.LEFT,
            GamepadInput.actionForKeyCode(KeyEvent.KEYCODE_DPAD_LEFT)
        )
        assertEquals(
            GamepadInput.Action.RIGHT,
            GamepadInput.actionForKeyCode(KeyEvent.KEYCODE_DPAD_RIGHT)
        )
        assertEquals(
            GamepadInput.Action.CONFIRM,
            GamepadInput.actionForKeyCode(KeyEvent.KEYCODE_BUTTON_A)
        )
        assertEquals(
            GamepadInput.Action.CANCEL,
            GamepadInput.actionForKeyCode(KeyEvent.KEYCODE_BUTTON_B)
        )
        assertNull(GamepadInput.actionForKeyCode(KeyEvent.KEYCODE_Z))
    }

    @Test
    fun actionForKeyCode_mapsMenuAndStartToPause() {
        assertEquals(
            GamepadInput.Action.PAUSE,
            GamepadInput.actionForKeyCode(KeyEvent.KEYCODE_BUTTON_START)
        )
        assertEquals(
            GamepadInput.Action.PAUSE,
            GamepadInput.actionForKeyCode(KeyEvent.KEYCODE_MENU)
        )
    }
}
