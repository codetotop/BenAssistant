package com.example.benassistant.llm

import android.content.Context

object LlmRouterFactory {

    fun create(context: Context): LlmRouter {
        val openAiClient: LLMClient = OpenAILLMClient()
        val ollamaClient: LLMClient = OllamaLLMClient()
        return LlmRouter(openAiClient, ollamaClient, context.applicationContext)
    }
}

