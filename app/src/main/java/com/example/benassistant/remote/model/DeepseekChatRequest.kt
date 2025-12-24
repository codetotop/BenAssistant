package com.example.benassistant.remote.model

data class DeepseekChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<ChatMessage>,
    val stream: Boolean = false
)
