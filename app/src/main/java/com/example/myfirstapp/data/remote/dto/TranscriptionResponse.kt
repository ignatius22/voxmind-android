package com.example.myfirstapp.data.remote.dto

data class ActionItemDto(
    val id: String,
    val task: String,
    val assignee: String? = null
)

data class SummarizeResponseDto(
    val noteId: String,
    val transcript: String,
    val summary: String,
    val keyTakeaways: List<String>,
    val actionItems: List<ActionItemDto>
)
