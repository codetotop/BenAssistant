package com.example.kaiaassistant.remote.ollama

import com.example.kaiaassistant.remote.model.OllamaChatRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApi {

    @POST("api/chat")
    suspend fun chatCompletions(@Body request: OllamaChatRequest): ResponseBody
}
