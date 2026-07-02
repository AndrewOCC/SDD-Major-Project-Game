package com.aocc.framework.implementation

import android.content.Context
import android.os.Build
import android.view.View
import com.aocc.framework.Input
import com.aocc.framework.Viewport

class AndroidInput(context: Context, view: View, viewport: Viewport) : Input {
    private val touchHandler: TouchHandler

    init {
        RotationHandler(context)

        touchHandler = if (Build.VERSION.SDK_INT < 5) {
            SingleTouchHandler(view, viewport)
        } else {
            MultiTouchHandler(view, viewport)
        }
    }

    fun updateViewport(viewport: Viewport) {
        touchHandler.setViewport(viewport)
    }

    override fun isTouchDown(pointer: Int): Boolean {
        return touchHandler.isTouchDown(pointer)
    }

    override fun getTouchX(pointer: Int): Int {
        return touchHandler.getTouchX(pointer)
    }

    override fun getTouchY(pointer: Int): Int {
        return touchHandler.getTouchY(pointer)
    }

    override val touchEvents: List<Input.TouchEvent>
        get() = touchHandler.touchEvents

    override fun getRotationX(): Float {
        return RotationHandler.getRotationX()
    }

    override fun getRotationY(): Float {
        return RotationHandler.getRotationY()
    }

    override fun getRotationZ(): Float {
        return RotationHandler.getRotationZ()
    }
}
