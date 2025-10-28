package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private enum class RecordState { Idle, Recording, Paused }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onDoneClick: () -> Unit = {}
) {
    var state by rememberSaveable { mutableStateOf(RecordState.Idle) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recording") },
                actions = { TextButton(onClick = onClose) { Text("Close") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (state != RecordState.Idle) {
                BottomBar(
                    isPaused = state == RecordState.Paused,
                    onPauseResume = {
                        state = if (state == RecordState.Recording) RecordState.Paused else RecordState.Recording
                    },
                    onDone = onDoneClick
                )
            }
        }
    ) { inner ->
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

                Text(
                    text = when (state) {
                        RecordState.Idle -> "Ready to record"
                        RecordState.Recording -> "Recording…"
                        RecordState.Paused -> "Paused"
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.weight(1f))

                if (state == RecordState.Idle) {
                    CenterCircleButton(label = "START") { state = RecordState.Recording }
                } else {
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

