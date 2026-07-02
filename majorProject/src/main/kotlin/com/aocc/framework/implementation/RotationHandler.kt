package com.aocc.framework.implementation

import com.aocc.majorproject.MajorProjectGame
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

// EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

class RotationHandler(context: Context?) : SensorEventListener {

    companion object {
        @JvmField
        var screenX: Float = 0f

        @JvmField
        var screenY: Float = 0f

        @JvmField
        var screenZ: Float = 0f

        private val ROTATION_VECTOR_AXIS_SWAP = arrayOf(
            intArrayOf(1, -1, 0, 1),
            intArrayOf(-1, -1, 1, 0),
            intArrayOf(-1, 1, 0, 1),
            intArrayOf(1, 1, 1, 0)
        )

        @JvmStatic
        fun getRotationX(): Float {
            return screenX
        }

        @JvmStatic
        fun getRotationY(): Float {
            return screenY
        }

        @JvmStatic
        fun getRotationZ(): Float {
            return screenZ
        }
    }

    init {
        if (context != null) {
            val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            if (manager != null) {
                @Suppress("DEPRECATION")
                if (manager.getSensorList(Sensor.TYPE_ORIENTATION).isNotEmpty()) {
                    @Suppress("DEPRECATION")
                    val rotationVector = manager.getSensorList(Sensor.TYPE_ORIENTATION)[0]
                    manager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // nothing to do here
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.values == null || event.values.size < 3) {
            return
        }

        var rotationIndex = MajorProjectGame.screenRotation
        if (rotationIndex < 0 || rotationIndex >= ROTATION_VECTOR_AXIS_SWAP.size) {
            rotationIndex = 0
        }

        val axisSwap = ROTATION_VECTOR_AXIS_SWAP[rotationIndex]
        // rolling device over its short side	(-180 => 180, increases over right side)
        screenX = axisSwap[1] * event.values[1]
        // rolling device over its long side 	(-180 => 180, increases over front side)
        screenY = event.values[2]
        // angle device is facing (0 is north)	(0 => 360)
        screenZ = axisSwap[0] * event.values[0]
    }

    fun setScreenX(screenX: Float) {
        RotationHandler.screenX = screenX
    }
}
