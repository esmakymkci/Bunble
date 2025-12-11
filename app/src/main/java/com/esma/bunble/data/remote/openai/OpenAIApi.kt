package com.esma.bunble.data.remote.openai

import com.esma.bunble.BuildConfig
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {

    @POST("v1/chat/completions")
    suspend fun getWordDetails(
        @Header("Authorization") apiKey: String = "Bearer ${BuildConfig.OPENAI_API_KEY}",
        @Body requestBody: OpenAIRequestBody
    ): JsonObject
}
