package com.example.kaiaassistant.remote

data class ChatCompletionRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.2
)

data class ChatMessage(
    val role: String, // system | user | assistant
    val content: String
)
