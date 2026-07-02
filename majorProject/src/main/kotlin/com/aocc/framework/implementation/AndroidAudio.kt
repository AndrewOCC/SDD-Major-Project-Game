package com.aocc.framework.implementation

import android.app.Activity
import android.content.res.AssetManager
import android.media.AudioManager
import android.media.SoundPool
import com.aocc.framework.Audio
import com.aocc.framework.Music
import com.aocc.framework.Sound
import java.io.IOException

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

class AndroidAudio(activity: Activity) : Audio {
    private val assets: AssetManager = activity.assets
    private val soundPool: SoundPool = SoundPool(20, AudioManager.STREAM_MUSIC, 0)

    init {
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC)
    }

    override fun createMusic(filename: String): Music {
        try {
            val assetDescriptor = assets.openFd(filename)
            return AndroidMusic(assetDescriptor)
        } catch (e: IOException) {
            throw RuntimeException("Couldn't load music '$filename'")
        }
    }

    override fun createSound(filename: String): Sound {
        try {
            val assetDescriptor = assets.openFd(filename)
            val soundId = soundPool.load(assetDescriptor, 0)
            return AndroidSound(soundPool, soundId)
        } catch (e: IOException) {
            throw RuntimeException("Couldn't load sound '$filename'")
        }
    }
}
