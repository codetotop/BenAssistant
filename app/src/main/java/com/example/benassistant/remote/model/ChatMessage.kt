package com.example.benassistant.remote.model

data class ChatMessage(
    val role: String, // system | user | assistant
    val content: String
)
