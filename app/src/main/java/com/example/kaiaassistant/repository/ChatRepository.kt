package com.example.kaiaassistant.repository

import com.example.kaiaassistant.room.ChatLog

interface ChatRepository {
    suspend fun getTodayLogs(): List<ChatLog>
    suspend fun getChatLogs(): List<ChatLog>

    suspend fun processUserMessage(message: String)
}
