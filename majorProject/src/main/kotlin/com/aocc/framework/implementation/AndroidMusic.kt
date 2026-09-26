package com.aocc.framework.implementation

import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import com.aocc.framework.Music
import java.io.IOException

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

class AndroidMusic(assetDescriptor: AssetFileDescriptor) : Music,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnVideoSizeChangedListener {

    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var isPrepared: Boolean = false

    init {
        try {
            mediaPlayer.setDataSource(
                assetDescriptor.fileDescriptor,
                assetDescriptor.startOffset,
                assetDescriptor.length
            )
            mediaPlayer.prepare()
            isPrepared = true
            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.setOnSeekCompleteListener(this)
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.setOnVideoSizeChangedListener(this)
        } catch (e: Exception) {
            throw RuntimeException("Couldn't load music")
        }
    }

    override fun dispose() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

    override fun isLooping(): Boolean {
        return mediaPlayer.isLooping
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun isStopped(): Boolean {
        return !isPrepared
    }

    override fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun play() {
        if (mediaPlayer.isPlaying) {
            return
        }

        try {
            synchronized(this) {
                if (!isPrepared) {
                    mediaPlayer.prepare()
                }
                mediaPlayer.start()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun setLooping(isLooping: Boolean) {
        mediaPlayer.isLooping = isLooping
    }

    override fun setVolume(volume: Float) {
        mediaPlayer.setVolume(volume, volume)
    }

    override fun stop() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            synchronized(this) {
                isPrepared = false
            }
        }
    }

    override fun onCompletion(player: MediaPlayer) {
        synchronized(this) {
            isPrepared = false
        }
    }

    override fun seekBegin() {
        mediaPlayer.seekTo(0)
    }

    override fun onPrepared(player: MediaPlayer) {
        synchronized(this) {
            isPrepared = true
        }
    }

    override fun onSeekComplete(player: MediaPlayer) {
    }

    override fun onVideoSizeChanged(player: MediaPlayer, width: Int, height: Int) {
    }
}
