package com.example.benassistant.llm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class LlmRouter(
    private val openAiClient: LLMClient,
    private val ollamaClient: LLMClient,
    context: Context
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    suspend fun chat(isForceOffline: Boolean, messages: List<LLMMessage>): String {
        return when {
            isForceOffline -> ollamaClient.chat(messages)
            isOnline() -> openAiClient.chat(messages)
            else -> ollamaClient.chat(messages)
        }
    }

    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false

        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

