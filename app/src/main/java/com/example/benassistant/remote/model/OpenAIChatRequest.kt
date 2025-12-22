package com.example.benassistant.remote.model

data class OpenAIChatRequest(
    val model: String = "gpt-4.1",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.2
)
