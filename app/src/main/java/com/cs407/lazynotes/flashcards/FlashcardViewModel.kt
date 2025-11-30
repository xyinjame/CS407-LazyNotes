package com.cs407.lazynotes.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FlashcardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cards: List<Flashcard> = emptyList()
)

class FlashcardViewModel : ViewModel() {

    private val generator: FlashcardGenerator = PerplexityFlashcardGenerator()

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState

    fun loadFlashcards(transcript: String) {
        _uiState.value = FlashcardUiState(isLoading = true)

        viewModelScope.launch {
            try {
                val cards = generator.generateFlashcards(transcript)
                _uiState.value = FlashcardUiState(
                    isLoading = false,
                    cards = cards,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = FlashcardUiState(
                    isLoading = false,
                    cards = emptyList(),
                    error = e.message ?: "Failed to generate flashcards"
                )
            }
        }
    }
}
