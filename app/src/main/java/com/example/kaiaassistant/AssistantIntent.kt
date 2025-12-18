package com.example.kaiaassistant

sealed class AssistantIntent {
    data class Chat(
        val message: String
    ) : AssistantIntent()

    data class SetAlarm(
        val hour: Int,
        val minute: Int,
        val label: String?
    ) : AssistantIntent()

    data class OpenMap(
        val destination: String
    ) : AssistantIntent()
}

