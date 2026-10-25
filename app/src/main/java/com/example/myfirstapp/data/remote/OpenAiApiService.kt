package com.example.myfirstapp.data.remote

import com.example.myfirstapp.data.remote.dto.ChatCompletionRequestDto
import com.example.myfirstapp.data.remote.dto.ChatCompletionResponseDto
import com.example.myfirstapp.data.remote.dto.WhisperResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OpenAiApiService {

    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody
    ): WhisperResponseDto

    @POST("v1/chat/completions")
    suspend fun analyzeTranscript(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequestDto
    ): ChatCompletionResponseDto
}
