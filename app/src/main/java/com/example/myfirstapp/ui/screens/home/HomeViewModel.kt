package com.example.myfirstapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstapp.domain.model.NoteCategory
import com.example.myfirstapp.domain.model.VoiceNote
import com.example.myfirstapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val notes: List<VoiceNote> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: NoteCategory = NoteCategory.ALL,
    val totalMinutesRecorded: Int = 0,
    val totalActionItemsPending: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(NoteCategory.ALL)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getNotes(),
        _searchQuery,
        _selectedCategory
    ) { notes, query, category ->
        val filteredNotes = notes.filter { note ->
            val matchesCategory = (category == NoteCategory.ALL || note.category == category)
            val matchesQuery = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.summary.contains(query, ignoreCase = true) ||
                    note.transcript.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }

        val totalSecs = notes.sumOf { it.durationSeconds }
        val pendingActions = notes.sumOf { note -> note.actionItems.count { !it.isCompleted } }

        HomeUiState(
            notes = filteredNotes,
            searchQuery = query,
            selectedCategory = category,
            totalMinutesRecorded = totalSecs / 60,
            totalActionItemsPending = pendingActions,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: NoteCategory) {
        _selectedCategory.value = category
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }
}
