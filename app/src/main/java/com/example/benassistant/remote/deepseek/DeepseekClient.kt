package com.example.benassistant.remote.deepseek

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DeepseekClient {

    fun create(apiKey: String): DeepseekApi {

        val client = OkHttpClient.Builder()
            .addInterceptor(DeepseekAuthInterceptor(apiKey))
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepseekApi::class.java)
    }
}

