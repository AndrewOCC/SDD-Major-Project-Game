package com.aocc.majorproject.input;

import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/** Queues gamepad / keyboard menu actions for the current screen. */
public final class GamepadInput {

    public enum Action {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        CONFIRM,
        CANCEL,
    }

    private final List<Action> pending = new ArrayList<>();

    public boolean onKeyDown(KeyEvent event) {
        Action action = mapKey(event.getKeyCode());
        if (action == null) {
            return false;
        }
        synchronized (pending) {
            pending.add(action);
        }
        return true;
    }

    public List<Action> consumeActions() {
        synchronized (pending) {
            if (pending.isEmpty()) {
                return List.of();
            }
            List<Action> copy = new ArrayList<>(pending);
            pending.clear();
            return copy;
        }
    }

    private static Action mapKey(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP -> Action.UP;
            case KeyEvent.KEYCODE_DPAD_DOWN -> Action.DOWN;
            case KeyEvent.KEYCODE_DPAD_LEFT -> Action.LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT -> Action.RIGHT;
            case KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
                    KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_SPACE -> Action.CONFIRM;
            case KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_ESCAPE,
                    KeyEvent.KEYCODE_BACK -> Action.CANCEL;
            default -> null;
        };
    }

    /** Package-visible for unit tests (Robolectric KeyEvent may not report key codes). */
    static Action actionForKeyCode(int keyCode) {
        return mapKey(keyCode);
    }
}
