package com.aocc.framework.implementation;

import com.aocc.majorproject.MajorProjectGame;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


// EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

public class RotationHandler implements SensorEventListener {
    static float screenX;
    static float screenY;
    static float screenZ;

    
    public RotationHandler(Context context) {		// sensor type changed from the accelerometer
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        
        if (manager.getSensorList(Sensor.TYPE_ORIENTATION).size() != 0) {
            Sensor rotationVector = manager.getSensorList(Sensor.TYPE_ORIENTATION).get(0);
            manager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }
    static final int ROTATION_VECTOR_AXIS_SWAP[][] = {
    	{1, -1, 0, 1}, {-1, -1, 1, 0}, {-1, 1, 0, 1},{1, 1, 1, 0}};
    	
   
    @Override
    public void onSensorChanged(SensorEvent event) {
        final int[] as = ROTATION_VECTOR_AXIS_SWAP[MajorProjectGame.screenRotation];
    	// rolling device over its short side	(-180 => 180, increases over right side)
        screenX = as[1]*event.values[1];
        // rolling device over its long side 	(-180 => 180, increases over front side)
        screenY = event.values[2];
        // angle device is facing (0 is north)	(0 => 360)
        screenZ = as[0]*event.values[0];	
        
        //Log.d("MajorProjectGame", "X: " + screenX);
        //Log.d("MajorProjectGame", "Y: " + screenY);
        //Log.d("MajorProjectGame", "Z: " + screenZ);
        
    }

    public static float getRotationX() {
        return screenX;
    }

    public static float getRotationY() {
        return screenY;
    }

    public static float getRotationZ() {
        return screenZ;
    }



	public void setScreenX(float screenX) {
		RotationHandler.screenX = screenX;
	}
}
