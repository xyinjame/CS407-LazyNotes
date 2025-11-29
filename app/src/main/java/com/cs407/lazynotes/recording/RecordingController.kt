package com.cs407.lazynotes.recording

import android.content.Context
import android.media.MediaRecorder
import java.io.File
import android.os.Environment

fun createAudioFile(context: Context): File {
    // App-specific external "Music" directory
    val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        ?: context.filesDir
    // Keep in "recordings" subfolder
    val recordingsDir = File(baseDir, "recordings").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    return File(recordingsDir, "recording_${System.currentTimeMillis()}.m4a")
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

