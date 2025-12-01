package com.cs407.lazynotes.flashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object FlashcardCache {
    private val cache = mutableMapOf<String, List<Flashcard>>()

    fun get(noteId: String): List<Flashcard>? = cache[noteId]

    fun put(noteId: String, cards: List<Flashcard>) {
        cache[noteId] = cards
    }
}

data class FlashcardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cards: List<Flashcard> = emptyList()
)

class FlashcardViewModel : ViewModel() {

    private val generator: FlashcardGenerator = PerplexityFlashcardGenerator()

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState: StateFlow<FlashcardUiState> = _uiState

    fun loadFlashcards(noteId: String, transcript: String) {
        viewModelScope.launch {
            // Check if cache already exists first
            val cached = FlashcardCache.get(noteId)
            if (cached != null) {
                _uiState.value = FlashcardUiState(
                    isLoading = false,
                    error = null,
                    cards = cached
                )
                return@launch
            }

            // No cached value, we call Perplexity and cache result
            _uiState.value = FlashcardUiState(isLoading = true)

            try {
                val cards = generator.generateFlashcards(transcript)

                FlashcardCache.put(noteId, cards)

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
