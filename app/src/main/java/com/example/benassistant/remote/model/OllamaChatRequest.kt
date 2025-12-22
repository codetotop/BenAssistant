package com.example.benassistant.remote.model

data class OllamaChatRequest(
    val model: String = "qwen2.5:3b",
    val messages: List<ChatMessage>,
    val stream: Boolean = true
)
