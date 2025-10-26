// RecordingScreen.kt
package com.example.recording

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onDoneClick: () -> Unit = {}
) {
    var isRecording by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recording") },
                actions = {
                    TextButton(onClick = onClose) {
                        Text("Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (isRecording) {
                BottomBarTextOnly(
                    onPause = onPauseClick,
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
                    text = if (isRecording) "Recording…" else "Ready to record",
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.weight(1f))

                // Center circular button: tap once to go Idle -> Recording
                CenterButtonTextOnly(
                    isRecording = isRecording,
                    onClick = { if (!isRecording) isRecording = true }
                )

                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CenterButtonTextOnly(
    isRecording: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(96.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 2.dp,
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(if (isRecording) "PAUSE" else "START")
    }
}

@Composable
private fun BottomBarTextOnly(
    onPause: () -> Unit,
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
            onClick = onPause,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("PAUSE", color = MaterialTheme.colorScheme.onError)
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

/* ---------- Preview ---------- */
@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun PreviewRecordingScreen() {
    MaterialTheme { RecordingScreen() }
}
