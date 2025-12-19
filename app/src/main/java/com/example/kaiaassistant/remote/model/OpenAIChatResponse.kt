package com.example.kaiaassistant.remote.model

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)

