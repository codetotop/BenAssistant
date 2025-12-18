package com.example.kaiaassistant.repository

import com.example.kaiaassistant.data.ChatLog
import com.example.kaiaassistant.data.ChatLogDao
import com.example.kaiaassistant.data.Role

interface ChatRepository {
    suspend fun getChatLogs(): List<ChatLog>

    suspend fun processUserMessage(message: String)
}
