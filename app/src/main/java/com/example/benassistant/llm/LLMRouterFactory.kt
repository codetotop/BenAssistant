package com.example.benassistant.llm

import android.content.Context

object LlmRouterFactory {

    fun create(context: Context): LlmRouter {
        val openAiClient: LLMClient = OpenAILLMClient()
        val deepseekLLMClient: LLMClient = DeepseekLLMClient()
        val ollamaClient: LLMClient = OllamaLLMClient()
        return LlmRouter(openAiClient, deepseekLLMClient, ollamaClient, context.applicationContext)
    }
}

