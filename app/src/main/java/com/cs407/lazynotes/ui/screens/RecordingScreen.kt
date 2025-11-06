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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private enum class RecordState { Idle, Recording, Paused }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit,
    onNavigateToFolderSelect: () -> Unit
) {
    var state by rememberSaveable { mutableStateOf(RecordState.Idle) }

    Scaffold(
        topBar = {
            // Top bar with title "New Recording" and a Close text button on the right
            TopAppBar(
                title = { Text("New Recording") },
                // Close button in the app bar
                actions = { TextButton(onClick = { onNavigateToHome() }) { Text("Close") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
                        state = if (state == RecordState.Recording) RecordState.Paused else RecordState.Recording
                    },
                    onDone = { onNavigateToFolderSelect() }
                )
            }
        }
    ) { inner ->
        // Main screen area under the app bar and above the bottom bar
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(inner)
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

                Spacer(Modifier.weight(1f))

                // Center circle button:
                // - Visible only in Idle state, labeled "START"
                // - Clicking it moves the screen into Recording state
                if (state == RecordState.Idle) {
                    CenterCircleButton(label = "START") { state = RecordState.Recording }
                } else {
                    // When Recording or Paused, hide the center button
                    Spacer(Modifier.height(96.dp))
                }

                Spacer(Modifier.weight(1f))
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
        modifier = Modifier.size(96.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
    ) {
        Text(label)
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
            .height(56.dp)
            .background(MaterialTheme.colorScheme.error),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onPauseResume,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(if (isPaused) "RESUME" else "PAUSE", color = MaterialTheme.colorScheme.onError)
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
                .fillMaxHeight()
        ) {
            Text("DONE", color = MaterialTheme.colorScheme.onError)
        }
    }
}

