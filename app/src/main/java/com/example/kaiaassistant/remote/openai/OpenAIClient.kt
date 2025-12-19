package com.example.kaiaassistant.remote.openai

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OpenAIClient {

    fun create(apiKey: String): OpenAIApi {

        val client = OkHttpClient.Builder()
            .addInterceptor(OpenAIAuthInterceptor(apiKey))
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }
}

