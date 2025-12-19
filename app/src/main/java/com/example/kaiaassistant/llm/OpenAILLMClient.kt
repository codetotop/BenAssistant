package com.example.kaiaassistant.llm

import com.example.kaiaassistant.BuildConfig
import com.example.kaiaassistant.remote.model.OpenAIChatRequest
import com.example.kaiaassistant.remote.model.ChatMessage
import com.example.kaiaassistant.remote.openai.OpenAIClient

class OpenAILLMClient : LLMClient {

    private val api = OpenAIClient.create(BuildConfig.OPENAI_API_KEY)

    override suspend fun chat(
        messages: List<LLMMessage>
    ): String {

        val response = api.chatCompletions(
            OpenAIChatRequest(
                model = "gpt-5.1",
                messages = messages.map {
                    ChatMessage(it.role, it.content)
                }
            )
        )

        val text = response.choices.first().message.content

        return text
    }
}


