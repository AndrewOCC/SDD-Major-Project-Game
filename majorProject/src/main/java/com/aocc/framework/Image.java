package com.aocc.framework;

import com.aocc.framework.Graphics.ImageFormat;

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

public interface Image {
    public int getWidth();
    public int getHeight();
    public ImageFormat getFormat();
    public void dispose();
}
