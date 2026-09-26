package com.aocc.framework.implementation

import android.view.MotionEvent
import android.view.View
import com.aocc.framework.Input
import com.aocc.framework.Pool
import com.aocc.framework.Viewport

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

class MultiTouchHandler(view: View, viewport: Viewport) : TouchHandler {

    companion object {
        private const val MAX_TOUCHPOINTS = 10
    }

    private val isTouched = BooleanArray(MAX_TOUCHPOINTS)
    private val touchX = IntArray(MAX_TOUCHPOINTS)
    private val touchY = IntArray(MAX_TOUCHPOINTS)
    private val id = IntArray(MAX_TOUCHPOINTS)
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
            val action = event.action and MotionEvent.ACTION_MASK
            val pointerIndex =
                (event.action and MotionEvent.ACTION_POINTER_INDEX_MASK) shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
            val pointerCount = event.pointerCount
            for (i in 0 until MAX_TOUCHPOINTS) {
                if (i >= pointerCount) {
                    isTouched[i] = false
                    id[i] = -1
                    continue
                }
                val pointerId = event.getPointerId(i)
                if (event.action != MotionEvent.ACTION_MOVE && i != pointerIndex) {
                    // if it's an up/down/cancel/out event, mask the id to see if we should process it for this touch
                    // point
                    continue
                }
                val touchEvent: Input.TouchEvent
                when (action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                        touchEvent = touchEventPool.newObject()
                        touchEvent.type = Input.TouchEvent.TOUCH_DOWN
                        touchEvent.pointer = pointerId
                        touchEvent.x = toWorldX(event.getX(i)).also { touchX[i] = it }
                        touchEvent.y = toWorldY(event.getY(i)).also { touchY[i] = it }
                        isTouched[i] = true
                        id[i] = pointerId
                        touchEventsBuffer.add(touchEvent)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                        touchEvent = touchEventPool.newObject()
                        touchEvent.type = Input.TouchEvent.TOUCH_UP
                        touchEvent.pointer = pointerId
                        touchEvent.x = toWorldX(event.getX(i)).also { touchX[i] = it }
                        touchEvent.y = toWorldY(event.getY(i)).also { touchY[i] = it }
                        isTouched[i] = false
                        id[i] = -1
                        touchEventsBuffer.add(touchEvent)
                    }

                    MotionEvent.ACTION_MOVE -> {
                        touchEvent = touchEventPool.newObject()
                        touchEvent.type = Input.TouchEvent.TOUCH_DRAGGED
                        touchEvent.pointer = pointerId
                        touchEvent.x = toWorldX(event.getX(i)).also { touchX[i] = it }
                        touchEvent.y = toWorldY(event.getY(i)).also { touchY[i] = it }
                        isTouched[i] = true
                        id[i] = pointerId
                        touchEventsBuffer.add(touchEvent)
                    }
                }
            }
            return true
        }
    }

    override fun isTouchDown(pointer: Int): Boolean {
        synchronized(this) {
            val index = getIndex(pointer)
            return index >= 0 && index < MAX_TOUCHPOINTS && isTouched[index]
        }
    }

    override fun getTouchX(pointer: Int): Int {
        synchronized(this) {
            val index = getIndex(pointer)
            return if (index < 0 || index >= MAX_TOUCHPOINTS) 0 else touchX[index]
        }
    }

    override fun getTouchY(pointer: Int): Int {
        synchronized(this) {
            val index = getIndex(pointer)
            return if (index < 0 || index >= MAX_TOUCHPOINTS) 0 else touchY[index]
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

    // returns the index for a given pointerId or -1 if no index.
    private fun getIndex(pointerId: Int): Int {
        for (i in 0 until MAX_TOUCHPOINTS) {
            if (id[i] == pointerId) {
                return i
            }
        }
        return -1
    }
}
