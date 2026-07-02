package com.aocc.framework.implementation;

import java.util.List;

import android.view.View.OnTouchListener;

import com.aocc.framework.Input.TouchEvent;

import com.aocc.framework.Viewport;

public interface TouchHandler extends OnTouchListener {
    void setViewport(Viewport viewport);

    public boolean isTouchDown(int pointer);
    
    public int getTouchX(int pointer);
    
    public int getTouchY(int pointer);
    
    public List<TouchEvent> getTouchEvents();
}
