package com.example.benassistant.llm

import com.example.benassistant.BuildConfig
import com.example.benassistant.remote.model.OpenAIChatRequest
import com.example.benassistant.remote.model.ChatMessage
import com.example.benassistant.remote.openai.OpenAIClient

class OpenAILLMClient : LLMClient {

    private val api = OpenAIClient.create(BuildConfig.OPENAI_API_KEY)

    override suspend fun chat(
        messages: List<LLMMessage>
    ): String {

        val response = api.chatCompletions(
            OpenAIChatRequest(
                model = "gpt-4.1",
                messages = messages.map {
                    ChatMessage(it.role, it.content)
                }
            )
        )

        val text = response.choices.first().message.content

        return text
    }
}


