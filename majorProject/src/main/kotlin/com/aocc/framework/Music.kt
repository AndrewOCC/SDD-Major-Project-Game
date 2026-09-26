package com.aocc.framework

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

interface Music {
    fun play()

    fun stop()

    fun pause()

    fun setLooping(looping: Boolean)

    fun setVolume(volume: Float)

    fun isPlaying(): Boolean

    fun isStopped(): Boolean

    fun isLooping(): Boolean

    fun dispose()

    fun seekBegin()
}
