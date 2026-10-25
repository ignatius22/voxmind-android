package com.example.myfirstapp.ui.screens.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstapp.domain.model.NoteCategory
import com.example.myfirstapp.domain.repository.NoteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordUiState(
    val title: String = "",
    val category: NoteCategory = NoteCategory.MEETING,
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Int = 0,
    val isProcessing: Boolean = false,
    val createdNoteId: String? = null
)

class RecordViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun onTitleChanged(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onCategorySelected(category: NoteCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun startRecording() {
        _uiState.update { it.copy(isRecording = true, isPaused = false) }
        startTimer()
    }

    fun pauseRecording() {
        _uiState.update { it.copy(isPaused = true) }
        timerJob?.cancel()
    }

    fun resumeRecording() {
        _uiState.update { it.copy(isPaused = false) }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun finishAndGenerateSummary(onSuccess: (String) -> Unit) {
        timerJob?.cancel()
        val currentState = _uiState.value
        _uiState.update { it.copy(isRecording = false, isProcessing = true) }

        viewModelScope.launch {
            val processedNote = repository.processAudioAndSummarize(
                title = currentState.title.ifBlank { "Voice Recording ${System.currentTimeMillis() % 1000}" },
                category = currentState.category,
                audioFilePath = "/simulated/audio/path.mp3",
                durationSeconds = currentState.elapsedSeconds
            )
            _uiState.update { it.copy(isProcessing = false, createdNoteId = processedNote.id) }
            onSuccess(processedNote.id)
        }
    }
}
