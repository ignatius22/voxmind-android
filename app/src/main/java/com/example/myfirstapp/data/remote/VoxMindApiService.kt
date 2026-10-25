package com.example.myfirstapp.data.remote

import com.example.myfirstapp.data.remote.dto.SummarizeResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VoxMindApiService {

    @Multipart
    @POST("api/v1/notes/transcribe-and-summarize")
    suspend fun transcribeAndSummarize(
        @Part audio: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("category") category: RequestBody
    ): SummarizeResponseDto
}
