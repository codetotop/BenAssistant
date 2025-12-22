package com.example.benassistant.remote.ollama

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OllamaClient {

    fun create(): OllamaApi {

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val ollamaApi = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("http://10.0.2.2:11434/") // emulator → localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaApi::class.java)

        return ollamaApi
    }
}

