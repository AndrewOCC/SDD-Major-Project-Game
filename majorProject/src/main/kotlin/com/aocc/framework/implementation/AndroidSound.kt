package com.aocc.framework.implementation

import android.media.SoundPool
import com.aocc.framework.Sound

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

class AndroidSound(private val soundPool: SoundPool, private val soundId: Int) : Sound {

    override fun play(volume: Float) {
        soundPool.play(soundId, volume, volume, 0, 0, 1f)
    }

    override fun dispose() {
        soundPool.unload(soundId)
    }
}
