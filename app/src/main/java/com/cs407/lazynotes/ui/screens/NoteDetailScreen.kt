package com.cs407.lazynotes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cs407.lazynotes.data.NoteRepository
import com.cs407.lazynotes.data.Preferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String?,
    onNavigateBack: () -> Unit,
    onGenerateFlashcards: (String) -> Unit
) {
    var note by remember { mutableStateOf<com.cs407.lazynotes.data.Note?>(null) }

    // Observe preference: true = show transcript, false = show summary
    val showTranscriptFirst by Preferences.showTranscriptFirst.collectAsState(initial = true)

    LaunchedEffect(noteId) {
        if (noteId != null) {
            note = NoteRepository.getNoteById(noteId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(note?.title ?: "Note Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            note?.let { noteDetail ->
                // Choose which text to show
                val label = if (showTranscriptFirst) "Transcript" else "Summary"
                val content = if (showTranscriptFirst) noteDetail.transcript else noteDetail.summary

                // Single chosen section
                Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    content ?: "No ${label.lowercase()} available.",
                    style = if (label == "Transcript") MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Audio File Section
                Text("Audio File", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(noteDetail.audioUri ?: "No audio file linked.", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(24.dp))

                // Button to generate flashcards: only uses the chosen content.
                Button(
                    onClick = { content?.let { onGenerateFlashcards(it) } },
                    enabled = !content.isNullOrBlank()
                ) {
                    Text("Generate Flashcards")
                }
            }
        }
    }
}