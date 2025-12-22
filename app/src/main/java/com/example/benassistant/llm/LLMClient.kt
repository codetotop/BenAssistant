package com.example.benassistant.llm

interface LLMClient {
    suspend fun chat(
        messages: List<LLMMessage>
    ): String
}

data class LLMMessage(
    val role: String, // system | user | assistant
    val content: String
)


