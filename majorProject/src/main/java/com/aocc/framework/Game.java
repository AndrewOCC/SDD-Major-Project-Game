package com.aocc.framework;

//THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

public interface Game {

    public Audio getAudio();

    public Input getInput();

    public FileIO getFileIO();

    public Graphics getGraphics();

    public void setScreen(Screen screen);

    public Screen getCurrentScreen();

    public Screen getInitScreen();
}
