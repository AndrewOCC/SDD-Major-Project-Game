package com.aocc.framework;

import java.util.List;

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

public interface Input {
    
    public static class TouchEvent {
        public static final int TOUCH_DOWN = 0;
        public static final int TOUCH_UP = 1;
        public static final int TOUCH_DRAGGED = 2;
        public static final int TOUCH_HOLD = 3;

        public int type;
        public int x, y;
        public int pointer;


    }

    public boolean isTouchDown(int pointer);

    public int getTouchX(int pointer);

    public int getTouchY(int pointer);

    public List<TouchEvent> getTouchEvents();
    
    public float getRotationX();

    public float getRotationY();

    public float getRotationZ();
}
 