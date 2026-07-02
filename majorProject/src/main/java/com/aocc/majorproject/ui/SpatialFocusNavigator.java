package com.aocc.majorproject.ui;

import com.aocc.majorproject.input.GamepadInput;

import java.util.List;

/**
 * Cone-weighted spatial focus navigation (similar to Android TV / Unity UI focus).
 * From the current item, picks the nearest focusable in the requested direction
 * whose center lies inside a forward-facing cone.
 */
public final class SpatialFocusNavigator {

    /** Minimum alignment with the search direction (cos 60° → 60° half-angle cone). */
    private static final float MIN_DIRECTION_COS = 0.5f;

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private SpatialFocusNavigator() {
    }

    public static Direction directionFrom(GamepadInput.Action action) {
        return switch (action) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case LEFT -> Direction.LEFT;
            case RIGHT -> Direction.RIGHT;
            default -> null;
        };
    }

    /**
     * @return index of the next focus item, or {@code currentIndex} if none in that direction
     */
    public static int findNext(int currentIndex, Direction direction, List<UiBounds> items) {
        if (items == null || items.isEmpty()) {
            return currentIndex;
        }
        currentIndex = clamp(currentIndex, items.size());
        UiBounds current = items.get(currentIndex);
        float originX = current.centerX();
        float originY = current.centerY();
        float dirX = directionVectorX(direction);
        float dirY = directionVectorY(direction);

        int bestIndex = currentIndex;
        float bestScore = Float.MAX_VALUE;

        for (int i = 0; i < items.size(); i++) {
            if (i == currentIndex) {
                continue;
            }
            UiBounds candidate = items.get(i);
            float dx = candidate.centerX() - originX;
            float dy = candidate.centerY() - originY;
            float distSq = dx * dx + dy * dy;
            if (distSq < 1f) {
                continue;
            }

            float dist = (float) Math.sqrt(distSq);
            float alignment = (dx * dirX + dy * dirY) / dist;
            if (alignment < MIN_DIRECTION_COS) {
                continue;
            }

            float along = dx * dirX + dy * dirY;
            float perpSq = distSq - along * along;
            float score = along * along * 100f + perpSq;

            if (score < bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private static float directionVectorX(Direction direction) {
        return switch (direction) {
            case LEFT -> -1f;
            case RIGHT -> 1f;
            default -> 0f;
        };
    }

    private static float directionVectorY(Direction direction) {
        return switch (direction) {
            case UP -> -1f;
            case DOWN -> 1f;
            default -> 0f;
        };
    }

    private static int clamp(int index, int size) {
        if (index < 0) {
            return 0;
        }
        if (index >= size) {
            return size - 1;
        }
        return index;
    }
}
