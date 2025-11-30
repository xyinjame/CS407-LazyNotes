package com.cs407.lazynotes.ui.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.repository.NetworkResult
import com.cs407.lazynotes.data.repository.Transcript
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// A simple data class to represent a note.
data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val folder: String
)

object NoteRepository {
    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> = _notes

    fun addNote(note: Note) {
        _notes.add(note)
    }
}

// The UI state now holds the full Transcript object upon success
sealed class PollingUiState {
    object Idle : PollingUiState()
    object Polling : PollingUiState()
    data class Success(val transcript: Transcript) : PollingUiState() // Changed to hold Transcript
    data class Error(val message: String) : PollingUiState()
    object Timeout : PollingUiState()
}

class FolderSelectViewModel(private val repository: FirefliesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<PollingUiState>(PollingUiState.Idle)
    val uiState: StateFlow<PollingUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    fun startPolling(clientRefId: String) {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            _uiState.update { PollingUiState.Polling }

            val maxSummaryAttempts = 5 // Try to get the summary 5 times
            var attempt = 0
            val totalMaxAttempts = 15 // Shorten total timeout to 5 minutes (15 * 20s = 300s)

            while (attempt < totalMaxAttempts) {
                when (val result = repository.getTranscript(clientRefId)) {
                    is NetworkResult.Success -> {
                        val transcript = result.data
                        // Check for summary first, within the first 5 attempts
                        if (attempt < maxSummaryAttempts && transcript.summary?.overview != null) {
                            _uiState.update { PollingUiState.Success(transcript) }
                            return@launch // Got summary, success!
                        }

                        // After 5 attempts, or if summary is still null, check for sentences
                        if (attempt >= maxSummaryAttempts) {
                            val rawText = transcript.sentences?.mapNotNull { it.raw_text }?.joinToString(" ")
                            if (!rawText.isNullOrBlank()) {
                                // Create a new Transcript object with a fallback summary
                                val fallbackTranscript = transcript.copy(
                                    summary = com.cs407.lazynotes.data.repository.TranscriptSummary(
                                        overview = rawText,
                                        actionItems = null,
                                        keywords = null,
                                        outline = null
                                    )
                                )
                                _uiState.update { PollingUiState.Success(fallbackTranscript) }
                                return@launch // Fallback to raw text, success!
                            }
                        }
                        // If we are here, it means we have neither summary nor sentences yet, so we keep polling.
                    }
                    is NetworkResult.Failure -> {
                        // Only show an error if it's not the expected "not yet in the list" message.
                        if (!result.message.contains("not yet in the list")) {
                            _uiState.update { PollingUiState.Error(result.message) }
                            return@launch
                        }
                    }
                }
                delay(20_000) // Wait 20 seconds before next poll to respect rate limits
                attempt++
            }
            _uiState.update { PollingUiState.Timeout } // Loop finished, timeout.
        }
    }

    fun saveNoteToFolder(title: String, folder: String) {
        val currentState = _uiState.value
        if (currentState is PollingUiState.Success) {
            val noteContent = currentState.transcript.summary?.overview ?: "No content available."
            val newNote = Note(title = title, content = noteContent, folder = folder)
            NoteRepository.addNote(newNote)
        }
    }

    companion object {
        fun provideFactory(repository: FirefliesRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FolderSelectViewModel(repository) as T
                }
            }
        }
    }
}
