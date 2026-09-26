package com.aocc.majorproject.input

import android.view.KeyEvent

/** Queues gamepad / keyboard menu actions for the current screen. */
class GamepadInput {

    enum class Action {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        CONFIRM,
        CANCEL,
    }

    private val pending = mutableListOf<Action>()

    fun onKeyDown(event: KeyEvent): Boolean {
        val action = mapKey(event.keyCode) ?: return false
        synchronized(pending) {
            pending.add(action)
        }
        return true
    }

    fun consumeActions(): List<Action> {
        synchronized(pending) {
            if (pending.isEmpty()) {
                return emptyList()
            }
            val copy = ArrayList(pending)
            pending.clear()
            return copy
        }
    }

    companion object {
        private fun mapKey(keyCode: Int): Action? {
            return when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> Action.UP
                KeyEvent.KEYCODE_DPAD_DOWN -> Action.DOWN
                KeyEvent.KEYCODE_DPAD_LEFT -> Action.LEFT
                KeyEvent.KEYCODE_DPAD_RIGHT -> Action.RIGHT
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_SPACE -> Action.CONFIRM
                KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_ESCAPE,
                KeyEvent.KEYCODE_BACK -> Action.CANCEL
                else -> null
            }
        }

        /** Package-visible for unit tests (Robolectric KeyEvent may not report key codes). */
        @JvmStatic
        fun actionForKeyCode(keyCode: Int): Action? {
            return mapKey(keyCode)
        }
    }
}
