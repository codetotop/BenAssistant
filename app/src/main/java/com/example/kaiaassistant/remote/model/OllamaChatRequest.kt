package com.example.kaiaassistant.remote.model

data class OllamaChatRequest(
    val model: String = "gpt-5.1",
    val messages: List<ChatMessage>,
    val stream: Boolean = true
)
