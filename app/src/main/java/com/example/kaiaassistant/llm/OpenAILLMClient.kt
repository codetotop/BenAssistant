package com.example.kaiaassistant.llm

import com.example.kaiaassistant.remote.ChatCompletionRequest
import com.example.kaiaassistant.remote.ChatMessage
import com.example.kaiaassistant.remote.OpenAIClient

class OpenAILLMClient(
    apiKey: String
) : LLMClient {

    private val api = OpenAIClient.create(apiKey)

    override suspend fun chat(
        messages: List<LLMMessage>
    ): String {

        val response = api.chatCompletion(
            ChatCompletionRequest(
                messages = messages.map {
                    ChatMessage(it.role, it.content)
                }
            )
        )

        val text = response.choices.first().message.content

        return text
    }
}


