package com.example.benassistant.repository

import com.example.benassistant.AssistantIntent
import com.example.benassistant.agent.AlarmAgent
import com.example.benassistant.agent.MapAgent
import com.example.benassistant.llm.LLMMessage
import com.example.benassistant.llm.LlmRouter
import com.example.benassistant.llm.LlmRouter.ConnectionMode
import com.example.benassistant.room.Alarm
import com.example.benassistant.room.ChatLog
import com.example.benassistant.room.ChatLogDao
import com.example.benassistant.room.Role
import org.json.JSONObject
import kotlin.collections.listOf

class ChatRepositoryImpl(
    private val llmRouter: LlmRouter,
    private val chatDao: ChatLogDao
) : ChatRepository {

    companion object {
        private const val SYSTEM_PROMPT = """
Bạn là Ben, một AI Assistant chạy trên Android.

Nhiệm vụ của bạn:
- Hiểu đúng ý định người dùng
- Chỉ thực hiện hành động khi người dùng YÊU CẦU RÕ RÀNG

Quy tắc phân loại:
- Nếu người dùng chỉ hỏi thông tin, xin gợi ý, tư vấn, khám phá
  → KHÔNG thực hiện hành động, chỉ trò chuyện.
- Chỉ thực hiện hành động khi người dùng dùng các động từ như:
  mở bản đồ, chỉ đường, đi đến, đi tới, dẫn tới, đặt báo thức, đặt giờ, hẹn giờ, báo thức lúc, nhắc tôi lúc.

Quy tắc trả lời:
- Nếu là trò chuyện hoặc gợi ý → trả về JSON với type = "chat".
- Nếu là hành động hệ thống → trả về JSON đúng format.
- Không thêm bất kỳ text nào ngoài JSON.
- Không giải thích quyết định.

Nếu thiếu thông tin để thực hiện hành động:
- Trả về type = "chat" để hỏi lại ngắn gọn.

Các format hợp lệ:

Chat:
{ "type": "chat", "message": "..." }

Set Alarm:
{ "type": "set_alarm", "hour": 7, "minute": 0, "label": "Đi làm" }

Open Map:
{ "type": "open_map", "destination": "Sân bay Nội Bài" }

"""

    }

    override suspend fun getChatLogs(): List<ChatLog> {
        return chatDao.getLogs()
    }

    override suspend fun clearAll() {
        chatDao.clearAll()
    }

    override suspend fun processUserMessage(message: String): ChatLog {
        // 1. Save user message
        chatDao.insert(
            ChatLog(
                role = Role.USER,
                message = message
            )
        )

        // 2. Handle intent + save assistant message
        val intent = getIntent(message)
        val chatLog: ChatLog
        when (intent) {
            is AssistantIntent.Chat -> {
                chatLog = ChatLog(
                    role = Role.ASSISTANT,
                    message = intent.message,
                    isNew = true
                )
            }

            is AssistantIntent.SetAlarm -> {
                chatLog = ChatLog(
                    role = Role.ASSISTANT,
                    message = "Ben đã đặt báo thức lúc %02d:%02d"
                        .format(intent.hour, intent.minute),
                    alarm = Alarm(label = intent.label ?: "", hour = intent.hour, intent.minute),
                    isNew = true
                )
            }

            is AssistantIntent.OpenMap -> {
                chatLog = ChatLog(
                    role = Role.ASSISTANT,
                    message = "Ben đang mở bản đồ tới ${intent.destination}",
                    destination = intent.destination,
                    isNew = true
                )
            }
        }

        chatDao.insert(chatLog)
        return chatLog
    }

    private suspend fun getIntent(message: String): AssistantIntent {
        // 1. Check for keywords before calling LLM
        val lowerMessage = message.lowercase()
        val alarmKeywords =
            listOf("báo thức", "đặt báo thức", "báo thức lúc", "hẹn giờ", "đặt giờ", "alarm")
        val isAlarm = alarmKeywords.any { lowerMessage.contains(it) }

        // 2. Check set alarm
        val hourRegex = Regex("(\\d{1,2})[:h](\\d{1,2})?")
        val match = hourRegex.find(lowerMessage)
        val hour = match?.groups?.get(1)?.value?.toIntOrNull()
        val minute = match?.groups?.get(2)?.value?.toIntOrNull() ?: 0

        if (isAlarm && hour != null) {
            return AssistantIntent.SetAlarm(hour = hour, minute = minute, label = "Báo thức")
        }

        // 3. Check open map
        /*val mapKeywords = listOf("bản đồ", "đường đi", "chỉ đường", "đến đâu", "đi tới", "map", )
        if(mapKeywords.any { lowerMessage.contains(it) }) {
            val destinationRegex = Regex("(tới|đến|đi tới|đi đến) (.+)")
            val match = destinationRegex.find(lowerMessage)
            val destination = match?.groups?.get(2)?.value
            if (destination != null) {
                return AssistantIntent.OpenMap(destination = destination)
            }
        }*/

        // 4. Call LLM if no specific intent is detected or parsing fails
        val response = llmRouter.chat(ConnectionMode.DEEPSEEK, buildPrompt())
        return parseIntent(response)
    }

    // Build prompt with system prompt + all saved chat logs
    private suspend fun buildPrompt(): List<LLMMessage> {
        val messages = mutableListOf<LLMMessage>()

        // System instruction first
        messages += LLMMessage(role = "system", content = SYSTEM_PROMPT)

        // Then chat logs
        val logs = chatDao.getLogs().takeLast(4)
        logs.forEach { log ->
            log.message?.let { msg ->
                val roleStr = when (log.role) {
                    Role.USER -> "user"
                    Role.ASSISTANT -> "assistant"
                }
                messages += LLMMessage(role = roleStr, content = msg)
            }
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
