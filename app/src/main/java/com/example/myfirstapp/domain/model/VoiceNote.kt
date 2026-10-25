package com.example.myfirstapp.domain.model

import java.util.Date

enum class ProcessingStatus {
    RECORDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class ActionItem(
    val id: String,
    val task: String,
    val assignee: String? = null,
    val isCompleted: Boolean = false
)

data class VoiceNote(
    val id: String,
    val title: String,
    val audioPath: String? = null,
    val durationSeconds: Int = 0,
    val category: NoteCategory = NoteCategory.MEETING,
    val transcript: String = "",
    val summary: String = "",
    val keyTakeaways: List<String> = emptyList(),
    val actionItems: List<ActionItem> = emptyList(),
    val createdAt: Date = Date(),
    val status: ProcessingStatus = ProcessingStatus.COMPLETED
)
