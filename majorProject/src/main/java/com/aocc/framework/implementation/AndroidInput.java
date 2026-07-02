package com.aocc.framework.implementation;

import java.util.List;

import android.content.Context;
import android.os.Build.VERSION;
import android.view.View;

import com.aocc.framework.Input;
import com.aocc.framework.Viewport;

public class AndroidInput implements Input {
    private final TouchHandler touchHandler;

    public AndroidInput(Context context, View view, Viewport viewport) {
        RotationHandler accelHandler = new RotationHandler(context);

        if (VERSION.SDK_INT < 5) {
            touchHandler = new SingleTouchHandler(view, viewport);
        } else {
            touchHandler = new MultiTouchHandler(view, viewport);
        }
    }

    public void updateViewport(Viewport viewport) {
        touchHandler.setViewport(viewport);
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
