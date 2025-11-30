package com.cs407.lazynotes.recording

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.repository.NetworkResult
import com.cs407.lazynotes.ui.screens.RecordingScreen
import kotlinx.coroutines.launch

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

                    // Handle the NetworkResult from the repository
                    when (val result = repository.processRecordingForTranscription(audioFile, noteTitle)) {
                        is NetworkResult.Success -> {
                            // On success, get the string data and navigate
                            val clientRefId = result.data
                            isProcessing = false
                            onNavigateToFolderSelect(clientRefId)
                        }
                        is NetworkResult.Failure -> {
                            // On failure, log the error and navigate with null
                            println("ERROR: Failed to start transcription job. Message: ${result.message}")
                            isProcessing = false
                            onNavigateToFolderSelect(null)
                        }
                    }
                }
            } else {
                // If the audio file itself is null, navigate with null
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
