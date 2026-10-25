package com.example.myfirstapp.domain.repository

import com.example.myfirstapp.domain.model.NoteCategory
import com.example.myfirstapp.domain.model.VoiceNote
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<VoiceNote>>
    suspend fun getNoteById(id: String): VoiceNote?
    suspend fun saveNote(note: VoiceNote)
    suspend fun deleteNote(id: String)
    suspend fun toggleActionItem(noteId: String, actionItemId: String)
    suspend fun processAudioAndSummarize(
        title: String,
        category: NoteCategory,
        audioFilePath: String,
        durationSeconds: Int
    ): VoiceNote
}
