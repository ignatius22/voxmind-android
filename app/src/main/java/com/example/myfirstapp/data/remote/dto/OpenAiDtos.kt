package com.example.myfirstapp.data.remote.dto

data class WhisperResponseDto(
    val text: String
)

data class ChatCompletionRequestDto(
    val model: String = "gpt-4o-mini",
    val response_format: ResponseFormatDto = ResponseFormatDto("json_object"),
    val messages: List<ChatMessageDto>
)

data class ResponseFormatDto(
    val type: String
)

data class ChatMessageDto(
    val role: String,
    val content: String
)

data class ChatCompletionResponseDto(
    val choices: List<ChatChoiceDto>
)

data class ChatChoiceDto(
    val message: ChatMessageDto
)

data class AiNoteAnalysisDto(
    val summary: String = "",
    val keyTakeaways: List<String> = emptyList(),
    val actionItems: List<ActionItemDto> = emptyList()
)
