package com.example.kaiaassistant.repository

import com.example.kaiaassistant.AssistantIntent
import com.example.kaiaassistant.agent.AlarmAgent
import com.example.kaiaassistant.agent.MapAgent
import com.example.kaiaassistant.llm.LLMMessage
import com.example.kaiaassistant.llm.LlmRouter
import com.example.kaiaassistant.room.ChatLog
import com.example.kaiaassistant.room.ChatLogDao
import com.example.kaiaassistant.room.Role
import org.json.JSONObject

class ChatRepositoryImpl(
    private val llmRouter: LlmRouter,
    private val alarmAgent: AlarmAgent,
    private val mapAgent: MapAgent,
    private val chatDao: ChatLogDao
) : ChatRepository {

    companion object {
        private const val SYSTEM_PROMPT = """
Bạn là Benjamin , một AI Assistant chạy trên Android.

Hãy phân tích yêu cầu và trả về JSON theo đúng 1 trong các format sau:

Chat:
{ "type": "chat", "message": "..." }

Set Alarm:
{ "type": "set_alarm", "hour": 7, "minute": 0, "label": "Đi làm" }

Open Map:
{ "type": "open_map", "destination": "Sân bay Nội Bài" }

Chỉ trả về JSON, không thêm giải thích.
"""

    }

    override suspend fun getChatLogs(): List<ChatLog> {
        return chatDao.getLogs()
    }

    override suspend fun clearAll() {
        chatDao.clearAll()
    }

    override suspend fun processUserMessage(message: String) {
        // 1. Save user message
        chatDao.insert(
            ChatLog(
                role = Role.USER,
                message = message
            )
        )

        // 2. Call LLM
        val response = llmRouter.chat( false, buildPrompt())
        val intent = parseIntent(response)

        // 3. Handle intent + save assistant message
        when (intent) {

            is AssistantIntent.Chat -> {
                chatDao.insert(
                    ChatLog(
                        role = Role.ASSISTANT,
                        message = intent.message
                    )
                )
            }

            is AssistantIntent.SetAlarm -> {
                alarmAgent.setAlarm(
                    intent.hour,
                    intent.minute,
                    intent.label
                )
                chatDao.insert(
                    ChatLog(
                        role = Role.ASSISTANT,
                        message = "Đã đặt báo thức lúc %02d:%02d"
                            .format(intent.hour, intent.minute)
                    )
                )
            }

            is AssistantIntent.OpenMap -> {
                mapAgent.openMap(intent.destination)
                chatDao.insert(
                    ChatLog(
                        role = Role.ASSISTANT,
                        message = "Đang mở bản đồ tới ${intent.destination}"
                    )
                )
            }
        }
    }

    // Build prompt with system prompt + all saved chat logs
    private suspend fun buildPrompt(): List<LLMMessage> {
        val messages = mutableListOf<LLMMessage>()

        // System instruction first
        messages += LLMMessage(role = "system", content = SYSTEM_PROMPT)

        // Then chat logs
        val logs = chatDao.getLogs().takeLast(4)
        logs.forEach { log ->
            val roleStr = when (log.role) {
                Role.USER -> "user"
                Role.ASSISTANT -> "assistant"
            }
            messages += LLMMessage(role = roleStr, content = log.message)
        }

        return messages
    }

    private fun parseIntent(response: String): AssistantIntent {
        val json = JSONObject(response)
        return when (json.getString("type")) {

            "set_alarm" -> AssistantIntent.SetAlarm(
                hour = json.getInt("hour"),
                minute = json.getInt("minute"),
                label = json.optString("label")
            )

            "open_map" -> AssistantIntent.OpenMap(
                destination = json.getString("destination")
            )

            else -> AssistantIntent.Chat(
                message = json.getString("message")
            )
        }
    }

    override suspend fun clearExpiredLogs() {
        // remove logs older than 24h
        val expiredTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        chatDao.deleteOlderThan(expiredTime)
    }
}
