package com.example.benassistant.remote.deepseek

import okhttp3.Interceptor
import okhttp3.Response

class DeepseekAuthInterceptor(
    private val apiKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        return chain.proceed(request)
    }
}

