package com.example.myfirstapp.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstapp.domain.model.VoiceNote
import com.example.myfirstapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteDetailUiState(
    val note: VoiceNote? = null,
    val selectedTab: Int = 0, // 0 = Summary, 1 = Action Items, 2 = Transcript
    val isPlayingAudio: Boolean = false,
    val playbackProgress: Float = 0f,
    val playbackSpeed: Float = 1.0f,
    val isLoading: Boolean = true
)

class NoteDetailViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val note = repository.getNoteById(noteId)
            _uiState.update { it.copy(note = note, isLoading = false) }
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun toggleActionItem(actionItemId: String) {
        val currentNote = _uiState.value.note ?: return
        viewModelScope.launch {
            repository.toggleActionItem(currentNote.id, actionItemId)
            loadNote(currentNote.id)
        }
    }

    fun toggleAudioPlayback() {
        _uiState.update { it.copy(isPlayingAudio = !it.isPlayingAudio) }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }
}
