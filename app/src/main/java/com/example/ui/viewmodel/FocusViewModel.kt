package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.SoundscapeEngine
import com.example.data.model.FocusPreset
import com.example.data.model.FocusSession
import com.example.data.model.SoundscapeChannel
import com.example.data.model.StudyDesk
import com.example.data.model.TeaBlend
import com.example.data.model.TeaCatalog
import com.example.data.repository.LunarRepository
import com.example.ui.state.UiState
import com.example.ui.state.asUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FocusTimerUiState(
    val intentionLabel: String = "Chapter IV Study",
    val selectedPreset: FocusPreset = FocusPreset.POMODORO_25,
    val totalSeconds: Int = FocusPreset.POMODORO_25.minutes * 60,
    val remainingSeconds: Int = FocusPreset.POMODORO_25.minutes * 60,
    val isRunning: Boolean = false,
    val selectedTea: TeaBlend = TeaCatalog.blends.first(),
    val steepRemainingSeconds: Int = TeaCatalog.blends.first().steepMinutes * 60,
    val isSteeping: Boolean = false,
    val steepReady: Boolean = false,
    val activeSoundscapes: Set<SoundscapeChannel> = emptySet(),
    val isLoggingSession: Boolean = false,
    val lastCompletedSummary: String? = null,
    val feedbackMessage: String? = null
) {
    val progressFraction: Float
        get() = if (totalSeconds > 0) {
            (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
        } else 0f

    val steepTotalSeconds: Int
        get() = selectedTea.steepMinutes * 60

    val steepProgressFraction: Float
        get() = if (steepTotalSeconds > 0) {
            (steepTotalSeconds - steepRemainingSeconds).toFloat() / steepTotalSeconds.toFloat()
        } else 0f
}

class FocusViewModel(
    private val repository: LunarRepository,
    private val currentUserId: String,
    initialFocusMinutes: Int = 25,
    initialTeaId: String = "earl_grey",
    initialIntention: String = "Chapter IV Study",
    private val soundscapeEngine: SoundscapeEngine = SoundscapeEngine()
) : ViewModel() {

    private val initialPreset = FocusPreset.byMinutesOrDefault(initialFocusMinutes)
    private val initialTea = TeaCatalog.byIdOrFirst(initialTeaId)

    private val _timerState = MutableStateFlow(
        FocusTimerUiState(
            intentionLabel = initialIntention.ifBlank { "Chapter IV Study" },
            selectedPreset = initialPreset,
            totalSeconds = initialPreset.minutes * 60,
            remainingSeconds = initialPreset.minutes * 60,
            selectedTea = initialTea,
            steepRemainingSeconds = initialTea.steepMinutes * 60
        )
    )
    val timerState: StateFlow<FocusTimerUiState> = _timerState.asStateFlow()

    val sessionsState: StateFlow<UiState<List<FocusSession>>> =
        repository.observeFocusSessions(currentUserId)
            .asUiState(TAG, "Unable to sync focus sessions")
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    private var focusTickJob: Job? = null
    private var steepTickJob: Job? = null

    fun updateIntentionLabel(label: String) {
        _timerState.update { it.copy(intentionLabel = label.take(120)) }
    }

    fun applyDeskSetup(desk: StudyDesk) {
        if (_timerState.value.isRunning) return
        val preset = FocusPreset.byMinutesOrDefault(desk.recommendedMinutes)
        val tea = TeaCatalog.byIdOrFirst(desk.pairedTeaId)
        steepTickJob?.cancel()
        _timerState.update {
            it.copy(
                intentionLabel = desk.defaultIntention,
                selectedPreset = preset,
                totalSeconds = preset.minutes * 60,
                remainingSeconds = preset.minutes * 60,
                selectedTea = tea,
                steepRemainingSeconds = tea.steepMinutes * 60,
                isSteeping = false,
                steepReady = false,
                feedbackMessage = "${desk.name} prepared with ${tea.name}."
            )
        }
    }

    fun selectPreset(preset: FocusPreset) {
        focusTickJob?.cancel()
        val seconds = preset.minutes * 60
        _timerState.update {
            it.copy(
                selectedPreset = preset,
                totalSeconds = seconds,
                remainingSeconds = seconds,
                isRunning = false
            )
        }
    }

    fun toggleFocusTimer() {
        val current = _timerState.value
        if (current.isRunning) {
            pauseFocusTimer()
        } else {
            startFocusTimer()
        }
    }

    private fun startFocusTimer() {
        focusTickJob?.cancel()
        _timerState.update { it.copy(isRunning = true, lastCompletedSummary = null) }
        focusTickJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _timerState.value
                if (!current.isRunning) break
                if (current.remainingSeconds > 1) {
                    _timerState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
                } else {
                    _timerState.update {
                        it.copy(remainingSeconds = 0, isRunning = false)
                    }
                    logCurrentSession(completedNaturally = true)
                    break
                }
            }
        }
    }

    fun pauseFocusTimer() {
        focusTickJob?.cancel()
        _timerState.update { it.copy(isRunning = false) }
    }

    fun resetFocusTimer() {
        focusTickJob?.cancel()
        _timerState.update {
            it.copy(
                remainingSeconds = it.totalSeconds,
                isRunning = false
            )
        }
    }

    fun selectTea(tea: TeaBlend) {
        steepTickJob?.cancel()
        _timerState.update {
            it.copy(
                selectedTea = tea,
                steepRemainingSeconds = tea.steepMinutes * 60,
                isSteeping = false,
                steepReady = false
            )
        }
    }

    fun toggleSteepTimer() {
        val current = _timerState.value
        if (current.isSteeping) {
            steepTickJob?.cancel()
            _timerState.update { it.copy(isSteeping = false) }
            return
        }
        val startingSeconds = if (current.steepReady || current.steepRemainingSeconds <= 0) {
            current.selectedTea.steepMinutes * 60
        } else {
            current.steepRemainingSeconds
        }
        steepTickJob?.cancel()
        _timerState.update {
            it.copy(
                steepRemainingSeconds = startingSeconds,
                isSteeping = true,
                steepReady = false
            )
        }
        steepTickJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _timerState.value
                if (!state.isSteeping) break
                if (state.steepRemainingSeconds > 1) {
                    _timerState.update { it.copy(steepRemainingSeconds = it.steepRemainingSeconds - 1) }
                } else {
                    _timerState.update {
                        it.copy(
                            steepRemainingSeconds = 0,
                            isSteeping = false,
                            steepReady = true,
                            feedbackMessage = "${state.selectedTea.name} is steeped and ready to sip."
                        )
                    }
                    break
                }
            }
        }
    }

    fun resetSteepTimer() {
        steepTickJob?.cancel()
        _timerState.update {
            it.copy(
                steepRemainingSeconds = it.selectedTea.steepMinutes * 60,
                isSteeping = false,
                steepReady = false
            )
        }
    }

    fun toggleSoundscape(channel: SoundscapeChannel) {
        _timerState.update { state ->
            val updated = if (channel in state.activeSoundscapes) {
                state.activeSoundscapes - channel
            } else {
                state.activeSoundscapes + channel
            }
            soundscapeEngine.updateChannels(updated)
            state.copy(activeSoundscapes = updated)
        }
    }

    fun muteAllSoundscapes() {
        soundscapeEngine.updateChannels(emptySet())
        _timerState.update { it.copy(activeSoundscapes = emptySet()) }
    }

    fun completeAndLogNow() {
        focusTickJob?.cancel()
        _timerState.update { it.copy(isRunning = false) }
        logCurrentSession(completedNaturally = false)
    }

    private fun logCurrentSession(completedNaturally: Boolean) {
        val state = _timerState.value
        if (state.isLoggingSession) return
        val elapsedMinutes = if (completedNaturally) {
            state.selectedPreset.minutes
        } else {
            val elapsedSec = (state.totalSeconds - state.remainingSeconds).coerceAtLeast(60)
            (elapsedSec / 60).coerceAtLeast(1)
        }

        viewModelScope.launch {
            _timerState.update { it.copy(isLoggingSession = true) }
            val result = repository.logFocusSession(
                userId = currentUserId,
                label = state.intentionLabel,
                durationMinutes = elapsedMinutes,
                teaName = state.selectedTea.name,
                teaSteepMinutes = state.selectedTea.steepMinutes,
                soundscapes = state.activeSoundscapes.map { it.title }
            )
            result.fold(
                onSuccess = {
                    val summary = "${elapsedMinutes}m on \"${state.intentionLabel.ifBlank { "Moonlit Study" }}\" with ${state.selectedTea.name}"
                    _timerState.update {
                        it.copy(
                            isLoggingSession = false,
                            remainingSeconds = it.totalSeconds,
                            lastCompletedSummary = summary,
                            feedbackMessage = "Session archived to Lunar Castle."
                        )
                    }
                },
                onFailure = { error ->
                    _timerState.update {
                        it.copy(
                            isLoggingSession = false,
                            feedbackMessage = error.message ?: "Could not archive session."
                        )
                    }
                }
            )
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteFocusSession(currentUserId, sessionId).fold(
                onSuccess = { _timerState.update { it.copy(feedbackMessage = "Session removed from log.") } },
                onFailure = { err -> _timerState.update { it.copy(feedbackMessage = err.message ?: "Failed to remove session.") } }
            )
        }
    }

    fun clearFeedback() {
        _timerState.update { it.copy(feedbackMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        focusTickJob?.cancel()
        steepTickJob?.cancel()
        soundscapeEngine.stop()
    }

    private companion object {
        const val TAG = "FocusVM"
    }
}
