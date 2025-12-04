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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.lazynotes.ui.theme.MainBackground
import com.cs407.lazynotes.ui.theme.TopBar
import com.cs407.lazynotes.R

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

    val primary = colorResource(id = R.color.primary_blue)
    val secondary = colorResource(id = R.color.secondary_teal)
    val accent = colorResource(id = R.color.accent_coral)
    val background = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Recording",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            Icons.Default.Home,
                            "Home",
                            tint = primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = textPrimary,
                    navigationIconContentColor = primary,
                    actionIconContentColor = primary
                ),
                modifier = Modifier.shadow(elevation = 2.dp)
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
    ) { paddingValues ->
        Box(
            modifier = modifier.fillMaxSize().padding(paddingValues).background(background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(32.dp))

                Text(
                    text = when {
                        isRecording -> "Recording…"
                        isPaused -> "Paused"
                        else -> "Ready to record"
                    },
                    color = textPrimary
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = primary
                )

                Spacer(Modifier.weight(1f))

                if (!isRecording && !isPaused) {
                    CenterCircleButton(label = "START", onClick = onStartClick, enabled = !isProcessing)
                } else {
                    Spacer(Modifier.height(120.dp))
                }

                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 16.dp),
                        color = secondary
                    )
                    Text(
                        text="Processing...",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = textSecondary
                    )
                }
                
                Spacer(Modifier.weight(1f))
            }

            if (showDoneConfirm) {
                AlertDialog(
                    onDismissRequest = { showDoneConfirm = false },
                    title = {
                        Text(
                            "Finish recording?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    },
                    text = {
                        Text(
                            "Are you sure you want to stop and save this recording?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textPrimary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showDoneConfirm = false
                            onDoneClick()
                        }) {
                            Text(
                                "Yes",
                                color = accent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDoneConfirm = false
                        }) {
                            Text(
                                "Cancel",
                                color = textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    containerColor = surface
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

    val accent = colorResource(id = R.color.accent_coral)

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = Modifier
            .size(140.dp)
            .shadow(elevation = 4.dp, shape = CircleShape),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = accent,
            contentColor = Color.White,
            disabledContainerColor = accent.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BottomBar(
    isPaused: Boolean,
    onPauseResumeClick: () -> Unit,
    onDoneClick: () -> Unit
) {

    val primary = colorResource(id = R.color.primary_blue)
    val accent = colorResource(id = R.color.accent_coral)
    val surface = colorResource(id = R.color.surface_white)
    val dividerColor = colorResource(id = R.color.divider_color)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onPauseResumeClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = Color.White
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (isPaused) "RESUME" else "PAUSE",
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onDoneClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(
                    "DONE",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
