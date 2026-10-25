package com.example.myfirstapp.ui.screens.detail

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstapp.domain.model.VoiceNote
import com.example.myfirstapp.domain.repository.NoteRepository
import com.example.myfirstapp.util.AndroidAudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

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

    private var audioPlayer: AndroidAudioPlayer? = null

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

    fun toggleAudioPlayback(context: Context) {
        val note = _uiState.value.note ?: return
        val currentlyPlaying = _uiState.value.isPlayingAudio

        if (currentlyPlaying) {
            audioPlayer?.pause()
            _uiState.update { it.copy(isPlayingAudio = false) }
        } else {
            val audioPath = note.audioPath ?: ""
            if (audioPath.isBlank()) {
                Toast.makeText(context, "No local audio recorded for this sample note", Toast.LENGTH_SHORT).show()
                return
            }

            val file = File(audioPath)
            if (file.exists() && file.length() > 0) {
                if (audioPlayer == null) {
                    audioPlayer = AndroidAudioPlayer(context.applicationContext)
                }
                _uiState.update { it.copy(isPlayingAudio = true) }
                audioPlayer?.playFile(
                    file = file,
                    speed = _uiState.value.playbackSpeed,
                    onCompletion = {
                        _uiState.update { it.copy(isPlayingAudio = false) }
                    }
                )
            } else {
                Toast.makeText(context, "Local audio file not found on device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
        audioPlayer?.setSpeed(speed)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer?.stop()
    }
}
