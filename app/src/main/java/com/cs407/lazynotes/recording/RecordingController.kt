package com.cs407.lazynotes.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import java.io.File

/**
 * Helper function to create a unique file path for the audio recording.
 * Recordings are saved in the app-specific external Music directory.
 */
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

    // Use a timestamp to ensure a unique filename
    return File(recordingsDir, "recording_${System.currentTimeMillis()}.m4a")
}

/**
 * Controls the lifecycle of the audio recording (start, pause, resume, stop).
 */
class RecordingController(private val context: Context) {
    private var recorder: MediaRecorder? = null
    var currentFile: File? = null
        private set

    /**
     * Starts a new audio recording session.
     */
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

    /**
     * Pauses the current recording.
     */
    fun pause() {
        try { recorder?.pause() } catch (_: Exception) {}
    }

    /**
     * Resumes a paused recording.
     */
    fun resume() {
        try { recorder?.resume() } catch (_: Exception) {}
    }

    /**
     * Stops the recording, cleans up resources, and returns the saved file.
     * This is used before uploading the file to Firebase.
     * * @return The saved audio File if successful, or null if stopping failed.
     */
    fun stop(): File? {
        val fileToReturn = currentFile
        try {
            recorder?.apply { stop(); reset(); release() }
        } catch (e: Exception) {
            // If recording stop fails, print error msg and delete the file
            println("Recording stop failed: ${e.message}")
            fileToReturn?.delete()
            return null
        } finally {
            recorder = null
            currentFile = null
        }
        // If succeed, return the file saved
        return fileToReturn
    }

    /**
     * Stops the recording and deletes the file immediately (used for Cancel/Home actions).
     */
    fun discard() {
        stop()
        currentFile?.delete()
        currentFile = null
    }
}