package com.aocc.framework

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

interface Input {

    class TouchEvent {
        @JvmField
        var type: Int = 0
        @JvmField
        var x: Int = 0
        @JvmField
        var y: Int = 0
        @JvmField
        var pointer: Int = 0

        companion object {
            const val TOUCH_DOWN = 0
            const val TOUCH_UP = 1
            const val TOUCH_DRAGGED = 2
            const val TOUCH_HOLD = 3
        }
    }

    fun isTouchDown(pointer: Int): Boolean

    fun getTouchX(pointer: Int): Int

    fun getTouchY(pointer: Int): Int

    val touchEvents: List<TouchEvent>

    fun getRotationX(): Float

    fun getRotationY(): Float

    fun getRotationZ(): Float
}
