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

    private val _audioUri = MutableStateFlow<String?>(null)

    // Expose the list of folders from the repository
    val folders = folderRepository.folders

    private var pollingJob: Job? = null

    fun updateNoteTitle(newTitle: String) {
        _noteTitle.value = newTitle
    }

    fun startPolling(clientRefId: String, audioUri: String?) {
        if (pollingJob?.isActive == true) return
        _audioUri.value = audioUri

        pollingJob = viewModelScope.launch {
            _uiState.value = PollingUiState.Polling
            val maxAttempts = 15
            val summaryPatienceAttempts = 10 // Try to get a summary for the first 10 attempts

            for (attempt in 0 until maxAttempts) {
                when (val result = firefliesRepository.getTranscript(clientRefId)) {
                    is NetworkResult.Success -> {
                        val transcript = result.data

                        // Check for summary first
                        val summaryReady = transcript.summary?.overview != null
                        if (summaryReady) {
                            _uiState.value = PollingUiState.Success(transcript)
                            _noteTitle.value = transcript.title ?: "Untitled Note"
                            return@launch // Success with summary
                        }

                        // If we're past the patience threshold, check for transcript only
                        if (attempt >= summaryPatienceAttempts) {
                            val transcriptReady = transcript.sentences?.isNotEmpty() == true
                            if (transcriptReady) {
                                _uiState.value = PollingUiState.Success(transcript)
                                _noteTitle.value = transcript.title ?: "Untitled Note"
                                return@launch // Success with transcript only
                            }
                        }
                    }
                    is NetworkResult.Failure -> {
                        // If it's not a 'not yet available' error, stop immediately.
                        if (!result.message.contains("not yet available")) {
                            _uiState.value = PollingUiState.Error(result.message)
                            return@launch
                        }
                    }
                }
                // Wait for 45 seconds before the next attempt
                delay(45_000)
            }
            _uiState.value = PollingUiState.Timeout
        }
    }


    fun saveNoteToFolder(folderName: String) {
        val currentState = _uiState.value
        if (currentState is PollingUiState.Success) {
            val transcript = currentState.transcript
            val title = _noteTitle.value.ifBlank { "Untitled Note" }
            val summary = transcript.summary?.overview
            // Combine all sentences to form the full transcript text
            val fullTranscript = transcript.sentences?.joinToString(separator = "\n") { it.raw_text ?: "" }
            val audioUri = _audioUri.value

            val newNote = Note(
                title = title,
                folderName = folderName,
                summary = summary,
                transcript = fullTranscript,
                audioUri = audioUri
            )
            noteRepository.addNote(newNote)
            println("SUCCESS: Saving note '$title' to folder '$folderName' with audio URI: $audioUri")
        }
    }

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
