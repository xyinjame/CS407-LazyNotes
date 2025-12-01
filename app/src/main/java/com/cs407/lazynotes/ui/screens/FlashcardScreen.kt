package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.lazynotes.flashcards.FlashcardViewModel
import com.cs407.lazynotes.flashcards.FlashcardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    noteId: String,
    transcript: String,
    onNavigateBack: () -> Unit,
    viewModel: FlashcardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(noteId, transcript) {
        if (!state.isLoading && state.cards.isEmpty() && state.error == null) {
            viewModel.loadFlashcards(noteId, transcript)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashcards") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${state.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                state.cards.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No flashcards generated")
                    }
                }

                else -> {
                    FlashcardPagerContent(state)
                }
            }
        }
    }
}

@Composable
private fun FlashcardPagerContent(state: FlashcardUiState) {
    var currentIndex by rememberSaveable { mutableStateOf(0) }
    var isFront by rememberSaveable { mutableStateOf(true) }

    val total = state.cards.size
    val card = state.cards[currentIndex]

    // Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (isFront) 0f else 180f,
        animationSpec = tween(durationMillis = 250),
        label = "flashcardFlip"
    )

    val questionScrollState = rememberScrollState()
    val answerScrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: progress text
        Text(
            text = "Card ${currentIndex + 1} of $total",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Middle: flashcard
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(16.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .clickable { isFront = !isFront },
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val showingFront = rotation <= 90f

                if (showingFront) {
                    // Front is Question
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 0f }
                    ) {
                        Text(
                            text = "Question",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(questionScrollState)
                        ) {
                            Text(
                                text = card.question,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Back is Answer
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        Text(
                            text = "Answer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(answerScrollState)
                        ) {
                            Text(
                                text = card.answer,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Hint text below card
        Text(
            text = "Tap the card to flip",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Bottom: previous / next controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex--
                        isFront = true
                    }
                },
                enabled = currentIndex > 0
            ) {
                Text("< Previous")
            }

            TextButton(
                onClick = {
                    if (currentIndex < total - 1) {
                        currentIndex++
                        isFront = true
                    }
                },
                enabled = currentIndex < total - 1
            ) {
                Text("Next >")
            }
        }
    }
}
