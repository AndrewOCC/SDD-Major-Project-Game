package com.aocc.framework.implementation

import android.view.MotionEvent
import android.view.View
import com.aocc.framework.Input
import com.aocc.framework.Pool
import com.aocc.framework.Viewport

class SingleTouchHandler(view: View, viewport: Viewport) : TouchHandler {

    private var isTouched: Boolean = false
    private var touchX: Int = 0
    private var touchY: Int = 0
    private val touchEventPool: Pool<Input.TouchEvent>
    private val touchEventsList = ArrayList<Input.TouchEvent>()
    private val touchEventsBuffer = ArrayList<Input.TouchEvent>()
    private var viewport: Viewport? = viewport

    init {
        touchEventPool = Pool(Pool.PoolObjectFactory { Input.TouchEvent() }, 100)
        view.setOnTouchListener(this)
    }

    override fun setViewport(viewport: Viewport) {
        this.viewport = viewport
    }

    private fun toWorldX(screenX: Float): Int {
        return viewport?.screenToWorldX(screenX) ?: screenX.toInt()
    }

    private fun toWorldY(screenY: Float): Int {
        return viewport?.screenToWorldY(screenY) ?: screenY.toInt()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        synchronized(this) {
            val touchEvent = touchEventPool.newObject()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchEvent.type = Input.TouchEvent.TOUCH_DOWN
                    isTouched = true
                }
                MotionEvent.ACTION_MOVE -> {
                    touchEvent.type = Input.TouchEvent.TOUCH_DRAGGED
                    isTouched = true
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    touchEvent.type = Input.TouchEvent.TOUCH_UP
                    isTouched = false
                }
            }

            touchEvent.x = toWorldX(event.x).also { touchX = it }
            touchEvent.y = toWorldY(event.y).also { touchY = it }
            touchEventsBuffer.add(touchEvent)

            return true
        }
    }

    override fun isTouchDown(pointer: Int): Boolean {
        synchronized(this) {
            return pointer == 0 && isTouched
        }
    }

    override fun getTouchX(pointer: Int): Int {
        synchronized(this) {
            return touchX
        }
    }

    override fun getTouchY(pointer: Int): Int {
        synchronized(this) {
            return touchY
        }
    }

    override val touchEvents: List<Input.TouchEvent>
        get() {
            synchronized(this) {
                val len = touchEventsList.size
                for (i in 0 until len) {
                    touchEventPool.free(touchEventsList[i])
                }
                touchEventsList.clear()
                touchEventsList.addAll(touchEventsBuffer)
                touchEventsBuffer.clear()
                return touchEventsList
            }
        }
}
