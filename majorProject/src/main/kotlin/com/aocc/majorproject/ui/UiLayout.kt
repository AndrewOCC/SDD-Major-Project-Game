package com.aocc.majorproject.ui

import com.aocc.framework.GameConstants

/** Helpers for placing UI within the 1280×720 world rectangle. */
object UiLayout {

    @JvmStatic
    fun centerX(width: Int): Int {
        return (GameConstants.WORLD_WIDTH - width) / 2
    }

    @JvmStatic
    fun centerY(height: Int): Int {
        return (GameConstants.WORLD_HEIGHT - height) / 2
    }

    @JvmStatic
    fun alignRight(width: Int, margin: Int): Int {
        return GameConstants.WORLD_WIDTH - width - margin
    }
}
