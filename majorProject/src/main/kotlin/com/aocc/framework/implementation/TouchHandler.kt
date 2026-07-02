package com.aocc.framework.implementation

import android.view.View
import com.aocc.framework.Input
import com.aocc.framework.Viewport

interface TouchHandler : View.OnTouchListener {
    fun setViewport(viewport: Viewport)

    fun isTouchDown(pointer: Int): Boolean

    fun getTouchX(pointer: Int): Int

    fun getTouchY(pointer: Int): Int

    val touchEvents: List<Input.TouchEvent>
}
