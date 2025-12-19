package com.example.kaiaassistant.remote.model

data class OpenAIChatRequest(
    val model: String = "gpt-5.1",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.2
)
