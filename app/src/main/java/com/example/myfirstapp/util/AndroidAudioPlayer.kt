package com.example.myfirstapp.util

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import java.io.File

class AndroidAudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playFile(
        file: File,
        speed: Float = 1.0f,
        onCompletion: () -> Unit = {}
    ) {
        stop()
        if (!file.exists()) return

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setSpeed(speed)
                setOnCompletionListener {
                    stop()
                    onCompletion()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resume() {
        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    fun setSpeed(speed: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mediaPlayer != null) {
                val params = mediaPlayer?.playbackParams
                if (params != null) {
                    params.speed = speed
                    mediaPlayer?.playbackParams = params
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
