package com.cs407.lazynotes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cs407.lazynotes.data.FolderRepository
import com.cs407.lazynotes.data.Note
import com.cs407.lazynotes.data.NoteRepository
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.repository.NetworkResult
import com.cs407.lazynotes.data.repository.Transcript
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// The UI state now holds the full Transcript object upon success
sealed class PollingUiState {
    object Idle : PollingUiState()
    object Polling : PollingUiState()
    data class Success(val transcript: Transcript) : PollingUiState()
    data class Error(val message: String) : PollingUiState()
    object Timeout : PollingUiState()
}

class FolderSelectViewModel(
    private val firefliesRepository: FirefliesRepository,
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PollingUiState>(PollingUiState.Idle)
    val uiState: StateFlow<PollingUiState> = _uiState.asStateFlow()

    private val _noteTitle = MutableStateFlow("")
    val noteTitle: StateFlow<String> = _noteTitle.asStateFlow()

    // Expose the list of folders from the repository
    val folders = folderRepository.folders

    private var pollingJob: Job? = null

    fun updateNoteTitle(newTitle: String) {
        _noteTitle.value = newTitle
    }

    fun startPolling(clientRefId: String) {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            _uiState.value = PollingUiState.Polling

            val maxAttempts = 15 // Poll for up to ~11 minutes
            var attempt = 0

            while (attempt < maxAttempts) {
                when (val result = firefliesRepository.getTranscript(clientRefId)) {
                    is NetworkResult.Success -> {
                        val transcript = result.data
                        val hasContent = transcript.summary?.overview != null || transcript.sentences?.any() == true

                        if (hasContent) {
                            val finalTranscript = if (transcript.summary?.overview == null) {
                                val rawText = transcript.sentences?.joinToString(" ") { it.raw_text ?: "" } ?: ""
                                transcript.copy(
                                    summary = com.cs407.lazynotes.data.repository.TranscriptSummary(
                                        overview = rawText,
                                        actionItems = transcript.summary?.actionItems,
                                        keywords = transcript.summary?.keywords,
                                        outline = transcript.summary?.outline
                                    )
                                )
                            } else {
                                transcript
                            }

                            _uiState.value = PollingUiState.Success(finalTranscript)
                            _noteTitle.value = finalTranscript.title ?: "Untitled Note"
                            return@launch // Polling successful
                        }
                    }
                    is NetworkResult.Failure -> {
                        if (!result.message.contains("not yet available")) {
                            _uiState.value = PollingUiState.Error(result.message)
                            return@launch
                        }
                    }
                }
                delay(45_000) // Wait 45 seconds
                attempt++
            }
            _uiState.value = PollingUiState.Timeout
        }
    }

    fun saveNoteToFolder(folderName: String) {
        val currentState = _uiState.value
        if (currentState is PollingUiState.Success) {
            val noteContent = currentState.transcript.summary?.overview ?: "No content available."
            val title = _noteTitle.value.ifBlank { "Untitled Note" }

            val newNote = Note(title = title, content = noteContent, folder = folderName)
            noteRepository.addNote(newNote) // Use the global repository
            println("SUCCESS: Saving note with title '$title' to folder '$folderName'")
        }
    }

    // Updated factory to provide all necessary repositories
    companion object {
        fun provideFactory(
            firefliesRepo: FirefliesRepository,
            folderRepo: FolderRepository,
            noteRepo: NoteRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FolderSelectViewModel(firefliesRepo, folderRepo, noteRepo) as T
                }
            }
        }
    }
}
