package com.example.kaiaassistant.repository

import com.example.kaiaassistant.AssistantIntent
import com.example.kaiaassistant.agent.AlarmAgent
import com.example.kaiaassistant.agent.MapAgent
import com.example.kaiaassistant.data.ChatLog
import com.example.kaiaassistant.data.ChatLogDao
import com.example.kaiaassistant.data.Role
import com.example.kaiaassistant.llm.LLMClient
import com.example.kaiaassistant.llm.LLMMessage
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId

class ChatRepositoryImpl(
    private val llmClient: LLMClient,
    private val alarmAgent: AlarmAgent,
    private val mapAgent: MapAgent,
    private val chatDao: ChatLogDao
) : ChatRepository {

    companion object {
        private const val SYSTEM_PROMPT = """
Bạn là Kaia, một AI Assistant chạy trên Android.

Hãy phân tích yêu cầu và trả về JSON theo đúng 1 trong các format sau:

Chat:
{ "type": "chat", "message": "..." }

Set Alarm:
{ "type": "set_alarm", "hour": 7, "minute": 0, "label": "Đi làm" }

Open Map:
{ "type": "open_map", "destination": "Bệnh viện Đại học Y" }

Chỉ trả về JSON, không thêm giải thích.
"""

    }

    fun todayRangeMillis(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return start to end
    }

    override suspend fun getTodayLogs() = todayRangeMillis().let { (start, end) ->
        chatDao.getTodayLogs(start, end)
    }

    override suspend fun getChatLogs(): List<ChatLog> {
        clearExpiredLogs()
        return chatDao.getLogs()
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
        val response = llmClient.chat(buildPrompt(message))
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

    private fun buildPrompt(userInput: String): List<LLMMessage> {
        return listOf(
            LLMMessage(
                role = "system",
                content = SYSTEM_PROMPT
            ),
            LLMMessage(
                role = "user",
                content = userInput
            )
        )
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

    private suspend fun clearExpiredLogs() {
        val expiredTime =
            System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        chatDao.deleteOlderThan(expiredTime)
    }
}
