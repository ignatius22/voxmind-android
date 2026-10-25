package com.example.myfirstapp.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()
            recorder = this
        }
    }

    fun pause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.pause()
        }
    }

    fun resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.resume()
        }
    }

    fun stop() {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder?.reset()
            recorder?.release()
            recorder = null
        }
    }
}
