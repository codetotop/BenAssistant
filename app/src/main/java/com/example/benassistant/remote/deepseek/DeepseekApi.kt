package com.example.benassistant.remote.deepseek

import com.example.benassistant.remote.model.ChatResponse
import com.example.benassistant.remote.model.DeepseekChatRequest
import com.example.benassistant.remote.model.OpenAIChatRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepseekApi {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Body request: DeepseekChatRequest
    ): ChatResponse
}
