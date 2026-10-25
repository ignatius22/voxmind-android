package com.example.myfirstapp.data.repository

import com.example.myfirstapp.data.remote.VoxMindApiService
import com.example.myfirstapp.domain.model.ActionItem
import com.example.myfirstapp.domain.model.NoteCategory
import com.example.myfirstapp.domain.model.ProcessingStatus
import com.example.myfirstapp.domain.model.VoiceNote
import com.example.myfirstapp.domain.repository.NoteRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import java.util.UUID

class NoteRepositoryImpl(
    private val apiService: VoxMindApiService? = null
) : NoteRepository {

    private val notesState = MutableStateFlow<List<VoiceNote>>(getInitialSampleNotes())

    override fun getNotes(): Flow<List<VoiceNote>> = notesState.asStateFlow()

    override suspend fun getNoteById(id: String): VoiceNote? {
        return notesState.value.find { it.id == id }
    }

    override suspend fun saveNote(note: VoiceNote) {
        notesState.update { currentList ->
            val index = currentList.indexOfFirst { it.id == note.id }
            if (index >= 0) {
                currentList.toMutableList().apply { set(index, note) }
            } else {
                listOf(note) + currentList
            }
        }
    }

    override suspend fun deleteNote(id: String) {
        notesState.update { currentList ->
            currentList.filterNot { it.id == id }
        }
    }

    override suspend fun toggleActionItem(noteId: String, actionItemId: String) {
        notesState.update { currentList ->
            currentList.map { note ->
                if (note.id == noteId) {
                    val updatedItems = note.actionItems.map { item ->
                        if (item.id == actionItemId) item.copy(isCompleted = !item.isCompleted)
                        else item
                    }
                    note.copy(actionItems = updatedItems)
                } else {
                    note
                }
            }
        }
    }

    override suspend fun processAudioAndSummarize(
        title: String,
        category: NoteCategory,
        audioFilePath: String,
        durationSeconds: Int
    ): VoiceNote {
        val newId = UUID.randomUUID().toString()

        val initialNote = VoiceNote(
            id = newId,
            title = title.ifBlank { "Voice Note ${notesState.value.size + 1}" },
            audioPath = audioFilePath,
            durationSeconds = durationSeconds,
            category = category,
            createdAt = Date(),
            status = ProcessingStatus.PROCESSING
        )

        // Add initial note in processing state
        saveNote(initialNote)

        // Simulate AI backend processing or call Retrofit service
        delay(2500)

        val processedNote = initialNote.copy(
            transcript = "In this meeting, the team discussed the Q3 product roadmap and mobile app development priorities. Key focus areas include building VoxMind AI with Jetpack Compose, MVVM Clean Architecture, and setting up backend REST APIs. John will lead backend API deployment, while Sarah handles mobile UI design and state management.",
            summary = "The team finalized the Q3 mobile app architecture, assigning VoxMind AI mobile build to Compose + MVVM and backend REST APIs to Python/Kotlin.",
            keyTakeaways = listOf(
                "Adopted Jetpack Compose and MVVM Clean Architecture for Android.",
                "Backend developer will expose REST API for audio transcription & AI summarization.",
                "Targeting MVP testing within 2 weeks."
            ),
            actionItems = listOf(
                ActionItem("1", "Deploy Whisper & LLM REST endpoints on backend", "Backend Lead"),
                ActionItem("2", "Connect VoxMind Android App to Retrofit API", "Mobile Dev"),
                ActionItem("3", "Finalize UI Theme and dynamic waveform recorder", "Mobile Dev")
            ),
            status = ProcessingStatus.COMPLETED
        )

        saveNote(processedNote)
        return processedNote
    }

    companion object {
        fun getInitialSampleNotes(): List<VoiceNote> {
            return listOf(
                VoiceNote(
                    id = "sample-1",
                    title = "Product Strategy & Architecture Review",
                    durationSeconds = 245,
                    category = NoteCategory.MEETING,
                    transcript = "We decided to build VoxMind AI using Jetpack Compose and Kotlin Coroutines. The app target is busy professionals needing quick voice summaries with action items.",
                    summary = "Key architectural decisions for VoxMind AI: Jetpack Compose frontend with MVVM Clean Architecture and REST backend.",
                    keyTakeaways = listOf(
                        "High demand for AI voice note apps on Android.",
                        "Focus on fast transcription and clean action item export."
                    ),
                    actionItems = listOf(
                        ActionItem("a1", "Create project scaffold with domain/data/ui layers", "Mobile Dev", true),
                        ActionItem("a2", "Integrate audio recording permission and ViewModel", "Mobile Dev", false)
                    ),
                    createdAt = Date(System.currentTimeMillis() - 3600000 * 3),
                    status = ProcessingStatus.COMPLETED
                ),
                VoiceNote(
                    id = "sample-2",
                    title = "AI Model & Backend Integration Notes",
                    durationSeconds = 118,
                    category = NoteCategory.BRAINSTORM,
                    transcript = "Backend will expose a Multipart POST endpoint for audio file upload, returning JSON with transcript, summary bullet points, and action items.",
                    summary = "REST API contract finalized for voice upload and AI processing.",
                    keyTakeaways = listOf(
                        "Use multipart request for audio uploads.",
                        "Support async webhooks or instant JSON response for short recordings."
                    ),
                    actionItems = listOf(
                        ActionItem("b1", "Define Swagger/OpenAPI spec for backend", "Backend Dev", false)
                    ),
                    createdAt = Date(System.currentTimeMillis() - 3600000 * 24),
                    status = ProcessingStatus.COMPLETED
                )
            )
        }
    }
}
