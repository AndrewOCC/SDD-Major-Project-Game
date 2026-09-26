package com.aocc.majorproject.ui

import com.aocc.majorproject.input.GamepadInput
import kotlin.math.sqrt

/**
 * Cone-weighted spatial focus navigation (similar to Android TV / Unity UI focus).
 * From the current item, picks the nearest focusable in the requested direction
 * whose center lies inside a forward-facing cone.
 */
object SpatialFocusNavigator {

    /** Minimum alignment with the search direction (cos 60° → 60° half-angle cone). */
    private const val MIN_DIRECTION_COS = 0.5f

    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }

    @JvmStatic
    fun directionFrom(action: GamepadInput.Action): Direction? {
        return when (action) {
            GamepadInput.Action.UP -> Direction.UP
            GamepadInput.Action.DOWN -> Direction.DOWN
            GamepadInput.Action.LEFT -> Direction.LEFT
            GamepadInput.Action.RIGHT -> Direction.RIGHT
            else -> null
        }
    }

    /**
     * @return index of the next focus item, or `currentIndex` if none in that direction
     */
    @JvmStatic
    fun findNext(currentIndex: Int, direction: Direction, items: List<UiBounds>?): Int {
        if (items.isNullOrEmpty()) {
            return currentIndex
        }
        var index = clamp(currentIndex, items.size)
        val current = items[index]
        val originX = current.centerX().toFloat()
        val originY = current.centerY().toFloat()
        val dirX = directionVectorX(direction)
        val dirY = directionVectorY(direction)

        var bestIndex = index
        var bestScore = Float.MAX_VALUE

        for (i in items.indices) {
            if (i == index) {
                continue
            }
            val candidate = items[i]
            val dx = candidate.centerX() - originX
            val dy = candidate.centerY() - originY
            val distSq = dx * dx + dy * dy
            if (distSq < 1f) {
                continue
            }

            val dist = sqrt(distSq)
            val alignment = (dx * dirX + dy * dirY) / dist
            if (alignment < MIN_DIRECTION_COS) {
                continue
            }

            val along = dx * dirX + dy * dirY
            val perpSq = distSq - along * along
            val score = along * along * 100f + perpSq

            if (score < bestScore) {
                bestScore = score
                bestIndex = i
            }
        }
        return bestIndex
    }

    private fun directionVectorX(direction: Direction): Float {
        return when (direction) {
            Direction.LEFT -> -1f
            Direction.RIGHT -> 1f
            else -> 0f
        }
    }

    private fun directionVectorY(direction: Direction): Float {
        return when (direction) {
            Direction.UP -> -1f
            Direction.DOWN -> 1f
            else -> 0f
        }
    }

    private fun clamp(index: Int, size: Int): Int {
        if (index < 0) {
            return 0
        }
        if (index >= size) {
            return size - 1
        }
        return index
    }
}
