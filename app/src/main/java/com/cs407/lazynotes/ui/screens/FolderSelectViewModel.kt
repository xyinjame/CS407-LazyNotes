package com.cs407.lazynotes.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cs407.lazynotes.data.FolderRepository
import com.cs407.lazynotes.data.Note
import com.cs407.lazynotes.data.NoteRepository
import com.cs407.lazynotes.data.repository.FirefliesRepository
import com.cs407.lazynotes.data.repository.NetworkResult
import com.cs407.lazynotes.data.repository.PerplexityRepository
import com.cs407.lazynotes.data.repository.Transcript
import com.cs407.lazynotes.data.repository.TranscriptSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the various states of the transcription polling process.
 */
sealed class PollingUiState {
    object Idle : PollingUiState() // Before polling starts
    object Polling : PollingUiState() // Actively polling for a transcript
    data class Success(val transcript: Transcript) : PollingUiState() // Successfully retrieved a transcript
    data class Error(val message: String) : PollingUiState() // An unrecoverable error occurred
    object Timeout : PollingUiState() // Polling timed out
}

/**
 * ViewModel for the FolderSelectScreen.
 * This class is responsible for polling for the transcript, managing the UI state,
 * and handling the logic to save the final note to a folder.
 */
class FolderSelectViewModel(
    private val firefliesRepository: FirefliesRepository,
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val perplexityRepository: PerplexityRepository
) : ViewModel() {

    // --- UI State Management ---
    private val _uiState = MutableStateFlow<PollingUiState>(PollingUiState.Idle)
    val uiState: StateFlow<PollingUiState> = _uiState.asStateFlow()

    private val _noteTitle = MutableStateFlow("")
    val noteTitle: StateFlow<String> = _noteTitle.asStateFlow()

    private val _audioUri = MutableStateFlow<String?>(null)

    // Expose the list of folders directly from the repository
    val folders = folderRepository.folders

    private var pollingJob: Job? = null

    fun updateNoteTitle(newTitle: String) {
        _noteTitle.value = newTitle
    }

    /**
     * Starts the polling process to fetch a transcript from the Fireflies API.
     * It implements a two-stage patience logic for retrieving the summary.
     * @param clientRefId The unique ID of the transcript to poll for.
     * @param audioUri The URI of the audio file to be saved with the note.
     */
    fun startPolling(clientRefId: String, audioUri: String?) {
        // Cancel any previous polling run
        pollingJob?.cancel()

        // Reset state for a new recording
        _uiState.value = PollingUiState.Polling
        _noteTitle.value = ""
        _audioUri.value = audioUri

        pollingJob = viewModelScope.launch {
            val maxAttempts = 15
            // CRITICAL: We wait 5 attempts for a summary before accepting a transcript-only note (approx 3.75 mins).
            val summaryPatienceAttempts = 5

            for (attempt in 0 until maxAttempts) {
                when (val result = firefliesRepository.getTranscript(clientRefId)) {
                    is NetworkResult.Success -> {
                        val transcript = result.data

                        // Stage 1: Check for a complete transcript with a summary.
                        val hasSentences = transcript.sentences?.isNotEmpty() == true

                        if (hasSentences) {
                            val transcriptText = transcript.sentences
                                ?.joinToString(" ") { it.raw_text ?: "" }
                                ?: ""

                            when (val summaryResult = perplexityRepository.generateSummary(transcriptText)) {
                                is NetworkResult.Success -> {
                                    val completeSummary = transcript.copy (
                                        summary = TranscriptSummary(
                                            overview = summaryResult.data,
                                            actionItems = null,
                                            keywords = null,
                                            outline = null,
                                        )
                                    )
                                    _uiState.value = PollingUiState.Success(completeSummary)
                                    _noteTitle.value = transcript.title ?: "Untitled Note"
                                    return@launch
                                }
                                is NetworkResult.Failure -> {
                                    _uiState.value = PollingUiState.Success(transcript)
                                    _noteTitle.value = transcript.title ?: "Untitled Note"
                                    return@launch
                                }
                            }
                        }
                    }
                    is NetworkResult.Failure -> {
                        // If it's a final error (not a 'still processing' message), stop polling.
                        if (!result.message.contains("not yet available")) {
                            _uiState.value = PollingUiState.Error(result.message)
                            return@launch
                        }
                    }
                }
                // Wait for 45 seconds before the next attempt. // If all attempts fail
                delay(45_000)
            }
            _uiState.value = PollingUiState.Timeout
        }
    }

    /**
     * Saves the successfully fetched transcript data as a new Note into the selected folder.
     * @param folderName The name of the folder to save the note in.
     */
    fun saveNoteToFolder(folderName: String) {
        val currentState = _uiState.value
        if (currentState is PollingUiState.Success) {
            val transcript = currentState.transcript
            val title = _noteTitle.value.ifBlank { "Untitled Note" }
            val summary = transcript.summary?.overview
            val fullTranscript = transcript.sentences?.joinToString(separator = "\n") { it.raw_text ?: "" }
            val audioUri = _audioUri.value

            // CRITICAL: Create the final Note object and add it to the repository.
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

    /**
     * Companion object to provide a ViewModelProvider.Factory for creating this ViewModel.
     * This allows us to pass repositories as constructor parameters.
     */
    companion object {
        fun provideFactory(
            firefliesRepo: FirefliesRepository,
            folderRepo: FolderRepository,
            noteRepo: NoteRepository,
            perplexityRepo: PerplexityRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FolderSelectViewModel(
                        firefliesRepo,
                        folderRepo,
                        noteRepo,
                        perplexityRepo
                    ) as T
                }
            }
        }
    }
}
