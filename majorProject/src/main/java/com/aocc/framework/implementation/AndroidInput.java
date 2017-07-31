package com.aocc.framework.implementation;

import java.util.List;

import android.content.Context;
import android.os.Build.VERSION;
import android.view.View;

import com.aocc.framework.implementation.RotationHandler;
import com.aocc.framework.Input;

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

public class AndroidInput implements Input {    
    TouchHandler touchHandler;
    RotationHandler accelHandler;

    public AndroidInput(Context context, View view, float scaleX, float scaleY) {
        accelHandler = new RotationHandler(context);

    	
        if(VERSION.SDK_INT < 5) 	// modified to SDK_INT from SDK; old code was deprecated
            touchHandler = new SingleTouchHandler(view, scaleX, scaleY);
        else
            touchHandler = new MultiTouchHandler(view, scaleX, scaleY);        
    }


    @Override
    public boolean isTouchDown(int pointer) {
        return touchHandler.isTouchDown(pointer);
    }

    @Override
    public int getTouchX(int pointer) {
        return touchHandler.getTouchX(pointer);
    }

    @Override
    public int getTouchY(int pointer) {
        return touchHandler.getTouchY(pointer);
    }

    @Override
    public List<TouchEvent> getTouchEvents() {
        return touchHandler.getTouchEvents();
    }
    
    @Override
    public float getRotationX() {
        return RotationHandler.getRotationX();
    }

    @Override
    public float getRotationY() {
        return RotationHandler.getRotationY();
    }

    @Override
    public float getRotationZ() {
        return RotationHandler.getRotationZ();
    }
    
}
