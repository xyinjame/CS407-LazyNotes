package com.cs407.lazynotes.flashcards

interface FlashcardGenerator {
    suspend fun generateFlashcards(transcript: String): List<Flashcard>
}
