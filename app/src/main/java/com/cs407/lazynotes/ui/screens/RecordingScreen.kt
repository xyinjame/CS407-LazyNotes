package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs407.lazynotes.ui.theme.MainBackground
import com.cs407.lazynotes.ui.theme.TopBar
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.material3.AlertDialog

/*
Recordings are saved here: Click on Device Explorer ->
/storage/emulated/0/Android/data/com.cs407.lazynotes/files/Music/recordings
Make sure you go into Extended Controls -> Microphone -> Enable Host Microphone Access to record audio
 */
private enum class RecordState { Idle, Recording, Paused }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToFolderSelect: () -> Unit,
    isProcessing: Boolean,
    onStartRecording: () -> Unit = {},
    onPause: () -> Unit = {},
    onResume: () -> Unit = {}
) {
    var state by rememberSaveable { mutableStateOf(RecordState.Idle) }
    var showDoneConfirm by rememberSaveable { mutableStateOf(false) }
    var elapsedSeconds by rememberSaveable { mutableStateOf(0) }

    // Timer pauses when recording pauses
    LaunchedEffect(state) {
        if (state == RecordState.Recording) {
            while (true) {
                delay(1_000)
                elapsedSeconds += 1
            }
        }
    }

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Recording",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToHome() }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE0E0E0)
                )
            )
        },
        bottomBar = {
            // Bottom red bar is shown after recording starts
            if (state != RecordState.Idle) {
                BottomBar(
                    isPaused = state == RecordState.Paused,
                    onPauseResume = {
                        // Toggle between Recording and Paused when user taps PAUSE / RESUME
                        state = if (state == RecordState.Recording) {
                            onPause()
                            RecordState.Paused
                        } else {
                            onResume()
                            RecordState.Recording
                        }
                    },
                    onDone = { showDoneConfirm = true }
                )
            }
        }
    ) { inner ->
        // Main screen area under the app bar and above the bottom bar
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
                .background(MainBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Status text near the top: "Ready to record", "Recording…", or "Paused"
                Text(
                    text = when (state) {
                        RecordState.Idle -> "Ready to record"
                        RecordState.Recording -> "Recording…"
                        RecordState.Paused -> "Paused"
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Timer
                Spacer(Modifier.height(8.dp))
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.weight(1f))

                // Center circle button:
                // - Visible only in Idle state, labeled "START"
                // - Clicking it moves the screen into Recording state
                if (state == RecordState.Idle) {
                    CenterCircleButton(label = "START") {
                        elapsedSeconds = 0
                        onStartRecording()
                        state = RecordState.Recording
                    }
                } else {
                    // When Recording or Paused, hide the center button
                    Spacer(Modifier.height(96.dp))
                }

                Spacer(Modifier.weight(1f))
            }

            if (showDoneConfirm) {
                AlertDialog(
                    onDismissRequest = { showDoneConfirm = false },
                    title = { Text("Finish recording?") },
                    text = {
                        Text("Are you sure you want to stop and save this recording?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDoneConfirm = false
                                onNavigateToFolderSelect()
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDoneConfirm = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CenterCircleButton(
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(120.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF6200EE)
        ),
    ) {
        Text(
            label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BottomBar(
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(TopBar),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onPauseResume,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(if (isPaused) "RESUME" else "PAUSE", color = Color.Black)
        }
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onError.copy(alpha = 0.25f))
        )
        TextButton(
            onClick = onDone,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Text("DONE", color = Color.Black)
        }
    }
}

