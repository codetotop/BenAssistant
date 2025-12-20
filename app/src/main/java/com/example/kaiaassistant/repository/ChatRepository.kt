package com.example.kaiaassistant.repository

import com.example.kaiaassistant.room.ChatLog

interface ChatRepository {
    suspend fun getChatLogs(): List<ChatLog>
    suspend fun clearExpiredLogs()
    suspend fun clearAll()

    suspend fun processUserMessage(message: String)
}
