package com.cs407.lazynotes.recording

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.repository.NetworkResult
import com.cs407.lazynotes.ui.screens.RecordingScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * This composable is the "smart" container for the recording feature.
 * It manages state, logic, and interactions with the system (like the recorder and repository).
 */
@Composable
fun RecordingRoute(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    repository: FirefliesRepository,
    onNavigateToFolderSelect: (clientRefId: String?, audioUri: String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- State Management ---
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var recorder: AudioRecorder? by remember { mutableStateOf(null) }

    // --- Timer Logic ---
    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (true) {
                delay(1_000)
                elapsedSeconds++
            }
        }
    }

    // --- Event Handlers ---
    val onStart = {
        recorder = AudioRecorder(context).apply { start() }
        isRecording = true
        isPaused = false
        elapsedSeconds = 0
    }

    val onPause = {
        recorder?.pause()
        isPaused = true
    }

    val onResume = {
        recorder?.resume()
        isPaused = false
    }

    // Explicitly define the lambda type as () -> Unit to resolve the type mismatch
    val onDone: () -> Unit = {
        isProcessing = true
        recorder?.stop()
        val audioFile = recorder?.getAudioFile()
        val audioUri = recorder?.getAudioUri()

        if (audioFile != null) {
            coroutineScope.launch {
                when (val result = repository.processRecordingForTranscription(audioFile, audioFile.nameWithoutExtension)) {
                    is NetworkResult.Success -> {
                        onNavigateToFolderSelect(result.data, audioUri.toString())
                    }
                    is NetworkResult.Failure -> {
                        Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                        isProcessing = false
                    }
                }
            }
        } else {
            Toast.makeText(context, "Error: No recording found.", Toast.LENGTH_SHORT).show()
            isProcessing = false
        }
    }

    // --- UI Layer ---
    RecordingScreen(
        isRecording = isRecording,
        isPaused = isPaused,
        isProcessing = isProcessing,
        timeText = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
        onStartClick = onStart,
        onPauseClick = onPause,
        onResumeClick = onResume,
        onDoneClick = onDone,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToHome = onNavigateToHome
    )

    // Clean up the recorder when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            recorder?.stop()
        }
    }
}
