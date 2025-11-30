package com.cs407.lazynotes.recording

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.repository.NetworkResult
import com.cs407.lazynotes.ui.screens.RecordingScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/*
RecordingRoute: Manages the recording lifecycle, handles mic permissions,
and orchestrates the asynchronous process of uploading the audio and starting
AI transcription after the user confirms 'DONE'.
*/
@Composable
fun RecordingRoute(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToFolderSelect: (String?) -> Unit,
    repository: FirefliesRepository
) {
    val context = LocalContext.current
    val controller = remember { RecordingController(context) }
    val scope = rememberCoroutineScope()

    var hasMic by remember { mutableStateOf(false) }
    val requestMic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasMic = granted }

    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        requestMic.launch(Manifest.permission.RECORD_AUDIO)
    }

    RecordingScreen(
        navController = navController,
        isProcessing = isProcessing,
        onNavigateToHome = {
            controller.discard()
            onNavigateToHome()
        },
        onNavigateToFolderSelect = onNavigateToFolderSelect@{
            if (isProcessing) return@onNavigateToFolderSelect

            val audioFile = controller.stop()

            if (audioFile != null) {
                isProcessing = true

                scope.launch {
                    val noteTitle = audioFile.nameWithoutExtension

                    // Save the recording to public storage first
                    saveRecordingToPublicDirectory(context, audioFile)

                    // Then, proceed with uploading for transcription
                    when (val result = repository.processRecordingForTranscription(audioFile, noteTitle)) {
                        is NetworkResult.Success -> {
                            val clientRefId = result.data
                            isProcessing = false
                            onNavigateToFolderSelect(clientRefId)
                        }
                        is NetworkResult.Failure -> {
                            println("ERROR: Failed to start transcription job. Message: ${result.message}")
                            isProcessing = false
                            onNavigateToFolderSelect(null)
                        }
                    }
                }
            } else {
                onNavigateToFolderSelect(null)
            }
        },
        onStartRecording = {
            if (hasMic) controller.start() else requestMic.launch(Manifest.permission.RECORD_AUDIO)
        },
        onPause = { controller.pause() },
        onResume = { controller.resume() }
    )
}

/**
 * Saves the recorded audio file to a public directory using MediaStore.
 * This makes the recording visible in other apps like file managers or music players.
 */
private suspend fun saveRecordingToPublicDirectory(context: Context, sourceFile: File) {
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, sourceFile.name)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4") // Assuming m4a format
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/LazyNotesRecordings")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(audioCollection, contentValues)
        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                println("SUCCESS: Recording saved to public directory at $uri")
            } catch (e: Exception) {
                println("ERROR: Failed to save recording to public directory. ${e.message}")
                // If saving fails, we should still proceed with the upload, so we just log the error.
                // To be robust, one might delete the failed entry from MediaStore.
                resolver.delete(uri, null, null)
            }
        } else {
            println("ERROR: Could not create MediaStore entry for recording.")
        }
    }
}
