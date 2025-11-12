package com.cs407.lazynotes.recording

import android.content.Context
import android.media.MediaRecorder
import java.io.File

private fun createAudioFile(context: Context): File {
    val dir = context.getExternalFilesDir(null) ?: context.filesDir
    return File(dir, "recording_${System.currentTimeMillis()}.m4a")
}

class RecordingController(private val context: Context) {
    private var recorder: MediaRecorder? = null
    var currentFile: File? = null
        private set

    fun start() {
        if (recorder != null) return
        val file = createAudioFile(context)
        currentFile = file

        val r = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = r
    }

    fun pause() {
        try { recorder?.pause() } catch (_: Exception) {}
    }

    fun resume() {
        try { recorder?.resume() } catch (_: Exception) {}
    }

    fun stop() {
        try { recorder?.apply { stop(); reset(); release() } } catch (_: Exception) {} finally {
            recorder = null
        }
    }

    fun discard() {
        stop()
        currentFile?.delete()
        currentFile = null
    }
}

