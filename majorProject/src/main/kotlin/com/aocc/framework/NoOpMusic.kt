package com.aocc.framework

class NoOpMusic : Music {

    private var volume: Float = 0f
    private var looping: Boolean = false

    override fun play() {
    }

    override fun stop() {
    }

    override fun pause() {
    }

    override fun setLooping(looping: Boolean) {
        this.looping = looping
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
    }

    override fun isPlaying(): Boolean {
        return false
    }

    override fun isStopped(): Boolean {
        return true
    }

    override fun isLooping(): Boolean {
        return looping
    }

    override fun dispose() {
    }

    override fun seekBegin() {
    }
}
