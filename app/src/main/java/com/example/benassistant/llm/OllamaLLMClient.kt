// Kotlin
package com.example.benassistant.llm

import com.example.benassistant.remote.model.ChatMessage
import com.example.benassistant.remote.model.OllamaChatRequest
import com.example.benassistant.remote.ollama.OllamaClient
import com.google.gson.Gson
import com.google.gson.JsonObject

class OllamaLLMClient : LLMClient {

    private val api = OllamaClient.create()
    private val gson = Gson()

    override suspend fun chat(messages: List<LLMMessage>): String {
        val request = OllamaChatRequest(
            model = "qwen2.5:3b",
            messages = messages.map { ChatMessage(it.role, it.content) },
            stream = true
        )

        val sb = StringBuilder()
        val body = api.chatCompletions(request)

        body.use { responseBody ->
            responseBody.charStream().buffered().useLines { lines ->
                lines.forEach { line ->
                    val json = line.trim()
                    if (json.isEmpty()) return@forEach
                    try {
                        val obj: JsonObject = gson.fromJson(json, JsonObject::class.java)
                        val content = obj.getAsJsonObject("message")?.get("content")?.asString
                        if (!content.isNullOrEmpty()) sb.append(content)
                    } catch (_: Exception) {
                    }
                }
            }
        }

        return sb.toString()
    }
}
