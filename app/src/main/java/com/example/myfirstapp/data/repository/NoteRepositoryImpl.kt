package com.example.myfirstapp.data.repository

import com.example.myfirstapp.data.remote.OpenAiApiService
import com.example.myfirstapp.data.remote.VoxMindApiService
import com.example.myfirstapp.data.remote.dto.AiNoteAnalysisDto
import com.example.myfirstapp.data.remote.dto.ChatCompletionRequestDto
import com.example.myfirstapp.data.remote.dto.ChatMessageDto
import com.example.myfirstapp.domain.model.ActionItem
import com.example.myfirstapp.domain.model.NoteCategory
import com.example.myfirstapp.domain.model.ProcessingStatus
import com.example.myfirstapp.domain.model.VoiceNote
import com.example.myfirstapp.domain.repository.NoteRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

class NoteRepositoryImpl(
    private val apiService: VoxMindApiService? = null,
    private var openAiApiKey: String? = null
) : NoteRepository {

    private val notesState = MutableStateFlow<List<VoiceNote>>(getInitialSampleNotes())
    private val gson = Gson()

    private val openAiApi: OpenAiApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAiApiService::class.java)
    }

    fun setOpenAiApiKey(key: String) {
        this.openAiApiKey = key
    }

    fun clearSampleNotes() {
        notesState.update { emptyList() }
    }

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

        saveNote(initialNote)

        val apiKey = openAiApiKey
        val file = File(audioFilePath)

        var finalTranscript = ""
        var finalSummary = ""
        var finalKeyTakeaways = listOf<String>()
        var finalActionItems = listOf<ActionItem>()

        if (!apiKey.isNullOrBlank() && file.exists() && file.length() > 0) {
            try {
                // 1. Transcribe audio with OpenAI Whisper
                val requestFile = file.asRequestBody("audio/m4a".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val modelBody = "whisper-1".toRequestBody("text/plain".toMediaTypeOrNull())

                val whisperResponse = openAiApi.transcribeAudio(
                    authorization = "Bearer $apiKey",
                    file = multipartBody,
                    model = modelBody
                )
                finalTranscript = whisperResponse.text

                // 2. Summarize & extract action items with GPT-4o-mini
                val prompt = """
                    Analyze the following transcript from a voice note titled "${initialNote.title}" under category "${category.displayName}".
                    Extract:
                    1. Summary: A concise summary of the recording.
                    2. Key Takeaways: A list of 2-4 key takeaways.
                    3. Action Items: A list of actionable items with task description and optional assignee.
                    
                    Respond ONLY with valid JSON in this exact structure:
                    {
                      "summary": "string",
                      "keyTakeaways": ["takeaway 1", "takeaway 2"],
                      "actionItems": [
                        {"id": "1", "task": "Task description", "assignee": "Assignee name"}
                      ]
                    }
                """.trimIndent()

                val chatRequest = ChatCompletionRequestDto(
                    model = "gpt-4o-mini",
                    messages = listOf(
                        ChatMessageDto("system", prompt),
                        ChatMessageDto("user", finalTranscript)
                    )
                )

                val chatResponse = openAiApi.analyzeTranscript("Bearer $apiKey", chatRequest)
                val jsonContent = chatResponse.choices.firstOrNull()?.message?.content ?: "{}"

                val parsedAnalysis = gson.fromJson(jsonContent, AiNoteAnalysisDto::class.java)
                finalSummary = parsedAnalysis.summary
                finalKeyTakeaways = parsedAnalysis.keyTakeaways
                finalActionItems = parsedAnalysis.actionItems.mapIndexed { idx, dto ->
                    ActionItem(
                        id = dto.id.ifBlank { "ai-$idx" },
                        task = dto.task,
                        assignee = dto.assignee,
                        isCompleted = false
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback / Dynamic generation if API key is not set or network fails
        if (finalTranscript.isBlank()) {
            val formattedTime = "${durationSeconds / 60}m ${durationSeconds % 60}s"
            finalTranscript = "Audio recording (${file.name}, duration: $formattedTime). Title: ${initialNote.title}. Recorded in category: ${category.displayName}."
            finalSummary = "Recorded a ${category.displayName.lowercase()} voice note titled \"${initialNote.title}\" lasting $formattedTime."
            finalKeyTakeaways = listOf(
                "Recorded voice audio duration: $formattedTime.",
                "Category assigned: ${category.displayName}.",
                "Audio file saved locally at ${file.name}."
            )
            finalActionItems = listOf(
                ActionItem("act-1", "Review recording transcript and add notes", "Me", false),
                ActionItem("act-2", "Share ${category.displayName} audio summary with team", "Me", false)
            )
        }

        val processedNote = initialNote.copy(
            transcript = finalTranscript,
            summary = finalSummary,
            keyTakeaways = finalKeyTakeaways,
            actionItems = finalActionItems,
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
