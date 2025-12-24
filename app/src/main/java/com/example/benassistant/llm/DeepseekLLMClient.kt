package com.example.benassistant.llm

import com.example.benassistant.BuildConfig
import com.example.benassistant.remote.deepseek.DeepseekClient
import com.example.benassistant.remote.model.ChatMessage
import com.example.benassistant.remote.model.DeepseekChatRequest

class DeepseekLLMClient : LLMClient {

    private val api = DeepseekClient.create(BuildConfig.DEEPSEEK_API_KEY)

    override suspend fun chat(
        messages: List<LLMMessage>
    ): String {

        val response = api.chatCompletions(
            DeepseekChatRequest(
                messages = messages.map {
                    ChatMessage(it.role, it.content)
                }
            )
        )

        val text = response.choices.first().message.content

        return text
    }
}


