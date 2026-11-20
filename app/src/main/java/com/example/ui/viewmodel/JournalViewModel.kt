package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.StudyNote
import com.example.data.repository.LunarRepository
import com.example.ui.state.UiState
import com.example.ui.state.asUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JournalViewModel(
    private val repository: LunarRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow("All")
    val selectedTag: StateFlow<String> = _selectedTag.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val rawNotes: StateFlow<UiState<List<StudyNote>>> =
        repository.observeNotes(currentUserId)
            .asUiState(TAG, "Unable to sync journal entries")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val filteredNotes: StateFlow<UiState<List<StudyNote>>> =
        combine(rawNotes, _searchQuery, _selectedTag) { state, query, tag ->
            when (state) {
                is UiState.Success -> {
                    val q = query.trim().lowercase()
                    val filtered = state.data.filter { note ->
                        val matchesTag = tag == "All" || note.tag.equals(tag, ignoreCase = true)
                        val matchesQuery = q.isEmpty() ||
                            note.title.lowercase().contains(q) ||
                            note.body.lowercase().contains(q) ||
                            note.tag.lowercase().contains(q)
                        matchesTag && matchesQuery
                    }
                    UiState.Success(filtered)
                }
                else -> state
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTag(tag: String) {
        _selectedTag.value = tag
    }

    fun saveNote(
        editingNoteId: String?,
        title: String,
        body: String,
        tag: String,
        pinned: Boolean
    ) {
        if (title.isBlank()) {
            _feedbackMessage.value = "Please add a heading for your journal entry."
            return
        }
        viewModelScope.launch {
            val result = if (editingNoteId.isNullOrBlank()) {
                repository.createNote(currentUserId, title, body, tag, pinned)
            } else {
                repository.updateNote(currentUserId, editingNoteId, title, body, tag, pinned)
            }
            result.fold(
                onSuccess = {
                    _feedbackMessage.value = if (editingNoteId.isNullOrBlank()) {
                        "Entry saved to The Secret Garden."
                    } else {
                        "Journal entry updated."
                    }
                },
                onFailure = { err ->
                    _feedbackMessage.value = err.message ?: "Failed to save journal entry."
                }
            )
        }
    }

    fun toggleNotePinned(note: StudyNote) {
        viewModelScope.launch {
            repository.toggleNotePinned(currentUserId, note.id, !note.pinned)
                .onFailure { err ->
                    _feedbackMessage.value = err.message ?: "Failed to update pin state."
                }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(currentUserId, noteId).fold(
                onSuccess = { _feedbackMessage.value = "Entry removed from journal." },
                onFailure = { err -> _feedbackMessage.value = err.message ?: "Failed to delete entry." }
            )
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    private companion object {
        const val TAG = "JournalVM"
    }
}
