package com.cs407.lazynotes.recording

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.IOException

/**
 * A helper class to manage audio recording using Android's MediaRecorder.
 * This class encapsulates the setup, control (start, pause, resume, stop),
 * and file management for recording audio.
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun getAudioFile(): File? = audioFile
    fun getAudioUri(): Uri? = audioFile?.let { Uri.fromFile(it) }

    /**
     * Starts a new recording session.
     * It sets up the MediaRecorder with the necessary configuration and creates a file to save the recording.
     */
    fun start() {
        // CRITICAL: Choose the correct MediaRecorder constructor based on Android version.
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        // Define the output file path in the app's external files directory.
        audioFile = File(
            context.getExternalFilesDir("Music/recordings"),
            "recording_${System.currentTimeMillis()}.mp3"
        )

        // CRITICAL: Configure the MediaRecorder with audio source, format, and encoder.
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)

            try {
                // Prepare and start the recording.
                prepare()
                start()
            } catch (e: IOException) {
                // Handle cases where the recorder fails to prepare (e.g., microphone in use).
                e.printStackTrace()
                recorder = null
            }
        }
    }

    /**
     * Pauses the current recording. Requires API level 24 (Nougat) or higher.
     */
    fun pause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.pause()
        }
    }

    /**
     * Resumes a paused recording. Requires API level 24 (Nougat) or higher.
     */
    fun resume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder?.resume()
        }
    }

    /**
     * Stops the recording, finalizes the file, and releases MediaRecorder resources.
     */
    fun stop() {
        try {
            // CRITICAL: stop() must be called before reset() and release() to finalize the file.
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            recorder = null
        } catch (e: Exception) {
            // Catch exceptions if stop() is called in an invalid state (e.g., already stopped).
            e.printStackTrace()
            recorder?.reset()
            recorder?.release()
            recorder = null
        }
    }
}
