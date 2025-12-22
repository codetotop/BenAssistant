package com.example.benassistant.remote.openai

import com.example.benassistant.remote.model.ChatResponse
import com.example.benassistant.remote.model.OpenAIChatRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun chatCompletions(
        @Body request: OpenAIChatRequest
    ): ChatResponse
}
