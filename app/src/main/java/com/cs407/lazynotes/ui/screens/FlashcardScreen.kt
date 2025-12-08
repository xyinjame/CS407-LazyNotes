package com.cs407.lazynotes.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs407.lazynotes.R
import com.cs407.lazynotes.flashcards.FlashcardUiState
import com.cs407.lazynotes.flashcards.FlashcardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    noteId: String,
    transcript: String,
    onNavigateBack: () -> Unit,
    viewModel: FlashcardViewModel = viewModel()
) {

    val primary = colorResource(id = R.color.primary_blue)
    val secondary = colorResource(id = R.color.secondary_teal)
    val accent = colorResource(id = R.color.accent_coral)
    val background = colorResource(id = R.color.background_light)
    val surface = colorResource(id = R.color.surface_white)
    val textPrimary = colorResource(id = R.color.text_primary)
    val textSecondary = colorResource(id = R.color.text_secondary)

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(noteId, transcript) {
        if (!state.isLoading && state.cards.isEmpty() && state.error == null) {
            viewModel.loadFlashcards(noteId, transcript)
        }
    }

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Flashcards",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                    titleContentColor = textPrimary,
                    navigationIconContentColor = primary
                ),
                modifier = Modifier.shadow(elevation = 2.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(background)
        ) {
            when {
                state.isLoading -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .width(300.dp)
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = secondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Generating flashcards...",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "This may take a moment",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .width(300.dp)
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(
                                    "Error",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    state.error ?: "Unknown error",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }

                state.cards.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .width(300.dp)
                                .padding(20.dp)
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(
                                    "No flashcards yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Flashcards will appear here once generated",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        FlashcardPagerContent(
                            state = state,
                            primary = primary,
                            accent = accent,
                            surface = surface,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardPagerContent(
    state: FlashcardUiState,
    primary: Color,
    accent: Color,
    surface: Color,
    textPrimary: Color,
    textSecondary: Color
) {
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Middle: flashcard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .clickable { isFront = !isFront }
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = surface
            ),
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
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
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                        Spacer(Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(questionScrollState)
                        ) {
                            Text(
                                text = card.question,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
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
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        Spacer(Modifier.height(16.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(answerScrollState)
                        ) {
                            Text(
                                text = card.answer,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Hint text below card
        Text(
            text = "Tap the card to flip",
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Bottom: previous / next controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex--
                        isFront = true
                    }
                },
                enabled = currentIndex > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = Color.White,
                    disabledContainerColor = textSecondary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "< Previous",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    if (currentIndex < total - 1) {
                        currentIndex++
                        isFront = true
                    }
                },
                enabled = currentIndex < total - 1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = Color.White,
                    disabledContainerColor = textSecondary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "Next >",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
