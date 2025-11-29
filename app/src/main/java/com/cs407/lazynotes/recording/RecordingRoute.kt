package com.cs407.lazynotes.recording

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.cs407.lazynotes.data.repository.FirefliesRepository // NEW IMPORT
import com.cs407.lazynotes.ui.screens.RecordingScreen
import kotlinx.coroutines.launch // NEW IMPORT

/*
RecordingRoute: Manages the recording lifecycle, handles mic permissions,
and orchestrates the asynchronous process of uploading the audio and starting
AI transcription after the user confirms 'DONE'.
*/
@Composable
fun RecordingRoute(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToFolderSelect: () -> Unit,
    // NEW ARGUMENT: Dependency injection placeholder for the repository
    repository: FirefliesRepository
) {
    val context = LocalContext.current
    val controller = remember { RecordingController(context) }
    val scope = rememberCoroutineScope() // NEW: Coroutine scope for async operations

    var hasMic by remember { mutableStateOf(false) }
    val requestMic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasMic = granted }

    // NEW STATE: Tracks if the app is currently uploading/calling the AI API
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        requestMic.launch(Manifest.permission.RECORD_AUDIO)
    }

    RecordingScreen(
        navController = navController,
        isProcessing = isProcessing, // NEW: Pass loading state to the UI
        onNavigateToHome = {
            controller.discard()
            onNavigateToHome()
        },
        onNavigateToFolderSelect = onNavigateToFolderSelect@{
            // Check if already processing or if stop failed
            if (isProcessing) return@onNavigateToFolderSelect

            val audioFile = controller.stop() // Stop recording and get the File

            if (audioFile != null) {
                isProcessing = true // Start showing loading indicator

                // Launch coroutine for network/heavy IO tasks (upload + API call)
                scope.launch {
                    val noteTitle = audioFile.nameWithoutExtension

                    // Call the Repository function which handles Firebase upload and Fireflies API
                    val clientRefId = repository.processRecordingForTranscription(audioFile, noteTitle)

                    isProcessing = false // Hide loading indicator

                    if (clientRefId != null) {
                        // Success: The transcription job has been submitted. Clean up local file.
                        // audioFile.delete() // <-- This line is now commented out
                        onNavigateToFolderSelect()
                    } else {
                        // Failure: Handle error (e.g., show a SnackBar)
                        println("ERROR: Failed to start transcription job. File: ${audioFile.name}")
                        onNavigateToFolderSelect() // Navigate regardless for now
                    }
                }
            } else {
                // Recording failed to stop cleanly
                onNavigateToFolderSelect()
            }
        },
        onStartRecording = {
            if (hasMic) controller.start() else requestMic.launch(Manifest.permission.RECORD_AUDIO)
        },
        onPause = { controller.pause() },
        onResume = { controller.resume() }
    )
}