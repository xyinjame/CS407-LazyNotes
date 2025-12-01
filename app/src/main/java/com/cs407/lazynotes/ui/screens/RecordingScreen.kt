package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.lazynotes.ui.theme.MainBackground
import com.cs407.lazynotes.ui.theme.TopBar

/**
 * A "dumb" UI component for the recording screen.
 * It only displays the state provided to it and forwards user interactions via callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    isPaused: Boolean,
    isProcessing: Boolean,
    timeText: String,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onDoneClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var showDoneConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recording", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { IconButton(onClick = onNavigateToHome) { Icon(Icons.Default.Home, "Home") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE0E0E0))
            )
        },
        bottomBar = {
            if (isRecording || isPaused) {
                BottomBar(
                    isPaused = isPaused,
                    onPauseResumeClick = { if (isPaused) onResumeClick() else onPauseClick() },
                    onDoneClick = { showDoneConfirm = true }
                )
            }
        }
    ) {
        Box(
            modifier = modifier.fillMaxSize().padding(it).background(MainBackground)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = when {
                        isRecording -> "Recording…"
                        isPaused -> "Paused"
                        else -> "Ready to record"
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))
                Text(text = timeText, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)

                Spacer(Modifier.weight(1f))

                if (!isRecording && !isPaused) {
                    CenterCircleButton(label = "START", onClick = onStartClick, enabled = !isProcessing)
                } else {
                    Spacer(Modifier.height(120.dp))
                }

                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.padding(top=16.dp))
                    Text(text="Processing...", modifier = Modifier.padding(top=8.dp))
                }
                
                Spacer(Modifier.weight(1f))
            }

            if (showDoneConfirm) {
                AlertDialog(
                    onDismissRequest = { showDoneConfirm = false },
                    title = { Text("Finish recording?") },
                    text = { Text("Are you sure you want to stop and save this recording?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDoneConfirm = false
                            onDoneClick()
                        }) { Text("Yes") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDoneConfirm = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
private fun CenterCircleButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = Modifier.size(120.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF6200EE)
        ),
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomBar(
    isPaused: Boolean,
    onPauseResumeClick: () -> Unit,
    onDoneClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(TopBar),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPauseResumeClick, modifier = Modifier.weight(1f).fillMaxHeight()) {
            Text(if (isPaused) "RESUME" else "PAUSE", color = Color.Black)
        }
        Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onError.copy(alpha = 0.25f)))
        TextButton(onClick = onDoneClick, modifier = Modifier.weight(1f).fillMaxHeight()) {
            Text("DONE", color = Color.Black)
        }
    }
}
