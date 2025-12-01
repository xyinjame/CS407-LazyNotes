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
    var isProcessing by remember { mutableStateOf(false) } // For showing a loading indicator
    var elapsedSeconds by remember { mutableStateOf(0) }
    var recorder: AudioRecorder? by remember { mutableStateOf(null) }

    // --- Timer Logic ---
    // CRITICAL: This LaunchedEffect runs the timer. It restarts whenever isRecording or isPaused changes.
    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (true) {
                delay(1_000) // wait for 1 second
                elapsedSeconds++
            }
        }
    }

    // --- Event Handlers ---
    val onStart = {
        // CRITICAL: A new AudioRecorder is created for each recording session.
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

    // CRITICAL: This is the handler for when the user finishes recording.
    val onDone: () -> Unit = {
        isProcessing = true
        recorder?.stop()
        val audioFile = recorder?.getAudioFile()
        val audioUri = recorder?.getAudioUri()

        // Upload the recording file regardless of its size, as long as it exists.
        if (audioFile != null) {
            coroutineScope.launch {
                // Initiate the transcription process via the repository.
                when (val result = repository.processRecordingForTranscription(audioFile, audioFile.nameWithoutExtension)) {
                    is NetworkResult.Success -> {
                        // On success, navigate to the folder selection screen.
                        onNavigateToFolderSelect(result.data, audioUri.toString())
                    }
                    is NetworkResult.Failure -> {
                        Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                        isProcessing = false
                    }
                }
            }
        } else {
            // Only show an error if the file was not created at all.
            Toast.makeText(context, "Error: No recording found.", Toast.LENGTH_LONG).show()
            isProcessing = false
        }
    }

    // --- UI Layer ---
    // This passes all the state and event handlers to the "dumb" RecordingScreen composable.
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

    // CRITICAL: This DisposableEffect ensures that the recorder is stopped and resources are released
    // when the user leaves the screen, preventing memory leaks.
    DisposableEffect(Unit) {
        onDispose {
            recorder?.stop()
        }
    }
}
