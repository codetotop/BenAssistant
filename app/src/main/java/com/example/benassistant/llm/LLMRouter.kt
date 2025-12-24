package com.example.benassistant.llm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class LlmRouter(
    private val openAiClient: LLMClient,
    private val deepseekClient: LLMClient,
    private val ollamaClient: LLMClient,
    context: Context
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    suspend fun chat(connectionMode: ConnectionMode, messages: List<LLMMessage>): String {
        return when (connectionMode) {
            ConnectionMode.OFFLINE -> ollamaClient.chat(messages)
            ConnectionMode.DEEPSEEK -> if (isOnline()) deepseekClient.chat(messages) else ollamaClient.chat(messages)
            ConnectionMode.OPENAI -> if (isOnline()) openAiClient.chat(messages) else ollamaClient.chat(messages)
        }
    }

    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    enum class ConnectionMode {
        OFFLINE, DEEPSEEK, OPENAI
    }
}
