package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DiamondMilestone
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

class TablesViewModel(
    private val repository: LunarRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val rawMilestones: StateFlow<UiState<List<DiamondMilestone>>> =
        repository.observeMilestones(currentUserId)
            .asUiState(TAG, "Unable to sync study milestones")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val filteredMilestones: StateFlow<UiState<List<DiamondMilestone>>> =
        combine(rawMilestones, _searchQuery) { state, query ->
            when (state) {
                is UiState.Success -> {
                    val q = query.trim().lowercase()
                    val filtered = state.data.filter { ms ->
                        q.isEmpty() ||
                            ms.title.lowercase().contains(q) ||
                            ms.linkedTea.lowercase().contains(q)
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

    fun addMilestone(title: String, linkedTea: String) {
        if (title.isBlank()) {
            _feedbackMessage.value = "Enter a milestone title first."
            return
        }
        viewModelScope.launch {
            repository.addMilestone(currentUserId, title, linkedTea).fold(
                onSuccess = { _feedbackMessage.value = "Milestone pinned to your study desk." },
                onFailure = { err -> _feedbackMessage.value = err.message ?: "Failed to add milestone." }
            )
        }
    }

    fun toggleMilestone(milestone: DiamondMilestone) {
        viewModelScope.launch {
            repository.toggleMilestone(currentUserId, milestone.id, !milestone.completed)
                .onFailure { err ->
                    _feedbackMessage.value = err.message ?: "Failed to update milestone."
                }
        }
    }

    fun deleteMilestone(milestoneId: String) {
        viewModelScope.launch {
            repository.deleteMilestone(currentUserId, milestoneId)
                .onFailure { err ->
                    _feedbackMessage.value = err.message ?: "Failed to delete milestone."
                }
        }
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    private companion object {
        const val TAG = "TablesVM"
    }
}
