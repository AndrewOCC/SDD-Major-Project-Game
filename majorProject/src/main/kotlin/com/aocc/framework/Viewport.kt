package com.aocc.framework

import android.graphics.Rect

/**
 * Maps between virtual world coordinates and the letterboxed on-screen area.
 */
class Viewport {

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

        val scaleX = viewWidth.toFloat() / GameConstants.WORLD_WIDTH
        val scaleY = viewHeight.toFloat() / GameConstants.WORLD_HEIGHT
        scale = minOf(scaleX, scaleY)

        val contentWidth = GameConstants.WORLD_WIDTH * scale
        val contentHeight = GameConstants.WORLD_HEIGHT * scale
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
        rect.right = Math.round(offsetX + GameConstants.WORLD_WIDTH * scale)
        rect.bottom = Math.round(offsetY + GameConstants.WORLD_HEIGHT * scale)
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
