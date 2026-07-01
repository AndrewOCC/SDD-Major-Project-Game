package com.aocc.framework;

public class NoOpMusic implements Music {

    private float volume;
    private boolean looping;

    @Override
    public void play() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return true;
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void seekBegin() {
    }
}
