package com.example.benassistant.repository

import com.example.benassistant.room.ChatLog

interface ChatRepository {
    suspend fun getChatLogs(): List<ChatLog>
    suspend fun clearExpiredLogs()
    suspend fun clearAll()

    suspend fun processUserMessage(message: String): ChatLog
}
