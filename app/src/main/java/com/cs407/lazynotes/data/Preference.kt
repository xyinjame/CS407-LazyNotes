package com.cs407.lazynotes.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * App-wide settings observed by Compose.
 * In-memory for now. Can be swapped to DataStore later.
 */
object Preferences {
    // Existing: true = Alphabetical, false = Recently Edited
    private val _folderSortAlphabetical = MutableStateFlow(false)
    val folderSortAlphabetical: StateFlow<Boolean> = _folderSortAlphabetical
    fun setFolderSortAlphabetical(value: Boolean) {
        _folderSortAlphabetical.value = value
    }

    // NEW: Default text layout — true = transcript first, false = summary first
    private val _showTranscriptFirst = MutableStateFlow(true)
    val showTranscriptFirst: StateFlow<Boolean> = _showTranscriptFirst

    // New: Default Folder State on Home
    // true = Open (show notes under each folder), false = Closed (show folders only)
    private val _folderDefaultOpen = MutableStateFlow(false)
    val folderDefaultOpen: StateFlow<Boolean> = _folderDefaultOpen
    fun setFolderDefaultOpen(value: Boolean) {
        _folderDefaultOpen.value = value
    }

    fun setShowTranscriptFirst(value: Boolean) {
        _showTranscriptFirst.value = value
    }
}