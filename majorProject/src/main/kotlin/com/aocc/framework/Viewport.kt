package com.aocc.framework

import android.graphics.Rect

/**
 * Maps between virtual world coordinates and the letterboxed on-screen area.
 * Defaults to the 1280×720 game world; the rear display uses its own portrait world.
 */
class Viewport(
    val worldWidth: Int = GameConstants.WORLD_WIDTH,
    val worldHeight: Int = GameConstants.WORLD_HEIGHT,
) {

    private var scale: Float = 1f
    private var offsetX: Float = 0f
    private var offsetY: Float = 0f
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0

    fun update(viewWidth: Int, viewHeight: Int) {
        if (viewWidth <= 0 || viewHeight <= 0) {
            return
        }

        this.viewWidth = viewWidth
        this.viewHeight = viewHeight

        val scaleX = viewWidth.toFloat() / worldWidth
        val scaleY = viewHeight.toFloat() / worldHeight
        scale = minOf(scaleX, scaleY)

        val contentWidth = worldWidth * scale
        val contentHeight = worldHeight * scale
        offsetX = (viewWidth - contentWidth) * 0.5f
        offsetY = (viewHeight - contentHeight) * 0.5f
    }

    fun screenToWorldX(screenX: Float): Int {
        return Math.round((screenX - offsetX) / scale)
    }

    fun screenToWorldY(screenY: Float): Int {
        return Math.round((screenY - offsetY) / scale)
    }

    fun getLetterboxDestRect(): Rect {
        val rect = Rect()
        rect.left = Math.round(offsetX)
        rect.top = Math.round(offsetY)
        rect.right = Math.round(offsetX + worldWidth * scale)
        rect.bottom = Math.round(offsetY + worldHeight * scale)
        return rect
    }

    fun getScale(): Float {
        return scale
    }

    fun getOffsetX(): Float {
        return offsetX
    }

    fun getOffsetY(): Float {
        return offsetY
    }

    fun getViewWidth(): Int {
        return viewWidth
    }

    fun getViewHeight(): Int {
        return viewHeight
    }
}
