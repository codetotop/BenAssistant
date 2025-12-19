package com.example.kaiaassistant.remote.openai

import com.example.kaiaassistant.remote.model.ChatResponse
import com.example.kaiaassistant.remote.model.OpenAIChatRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun chatCompletions(
        @Body request: OpenAIChatRequest
    ): ChatResponse
}
