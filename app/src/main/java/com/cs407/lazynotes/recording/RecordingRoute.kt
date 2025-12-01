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

            isProcessing = true // Immediately update UI to processing state

            scope.launch(Dispatchers.IO) { // Move all work to a background thread
                val audioFile = controller.stop()

                if (audioFile != null) {
                    val noteTitle = audioFile.nameWithoutExtension

                    // Save the recording to public storage
                    saveRecordingToPublicDirectory(context, audioFile)

                    // Proceed with uploading for transcription
                    val result = repository.processRecordingForTranscription(audioFile, noteTitle)

                    // Switch back to the main thread to update UI and navigate
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is NetworkResult.Success -> {
                                val clientRefId = result.data
                                onNavigateToFolderSelect(clientRefId)
                            }
                            is NetworkResult.Failure -> {
                                println("ERROR: Failed to start transcription job. Message: ${result.message}")
                                onNavigateToFolderSelect(null)
                            }
                        }
                        isProcessing = false // Set processing to false after navigation
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isProcessing = false
                        onNavigateToFolderSelect(null)
                    }
                }
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
    // This function is already running on Dispatchers.IO because of the calling context
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
            resolver.delete(uri, null, null)
        }
    } else {
        println("ERROR: Could not create MediaStore entry for recording.")
    }
}
