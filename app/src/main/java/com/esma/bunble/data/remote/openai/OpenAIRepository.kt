package com.esma.bunble.data.remote.openai

import com.esma.bunble.BuildConfig
import com.esma.bunble.domain.model.Word
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAIRepository @Inject constructor(
    private val openAIApi: OpenAIApi,
    private val okHttpClient: OkHttpClient
) {

    suspend fun getWordDetails(
        sourceText: String,
        sourceLang: String,
        targetLang: String
    ): Word {
        val prompt = """
            Translate the word/phrase "$sourceText" from $sourceLang to $targetLang.
            Provide the following information in a valid JSON format, with no extra text or explanations:
            1. "translatedText": The translation.
            2. "phonetic": A simple phonetic transcription for the translation.
            3. "examples": An array of three simple, distinct example sentences using the translated word/phrase.
        """

        val requestBody = OpenAIRequestBody(
            model = "gpt-4o",
            messages = listOf(Message(role = "user", content = prompt)),
            response_format = ResponseFormat(type = "json_object")
        )

        try {
            val response = openAIApi.getWordDetails(
                apiKey = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                requestBody = requestBody
            )
            // Gelen JSON'ı parse etme
            val content = response
                .getAsJsonArray("choices")[0]
                .asJsonObject.getAsJsonObject("message")
                .get("content").asString

            val gson = Gson()
            val aiResponse: Map<String, Any> = gson.fromJson(content, object : TypeToken<Map<String, Any>>() {}.type)

            return Word(
                sourceText = sourceText,
                translatedText = aiResponse["translatedText"] as? String ?: "",
                phonetic = aiResponse["phonetic"] as? String,
                examples = aiResponse["examples"] as? List<String> ?: emptyList()
            )

        } catch (e: Exception) {
            throw Exception("Failed to get details from OpenAI: ${e.message}")
        }
    }

    suspend fun getChatResponse(chatHistory: List<Message>): String {
        val requestBody = OpenAIRequestBody(
            model = "gpt-4o",
            messages = chatHistory,
            response_format = null
        )

        try {
            val response = openAIApi.getChatResponse(
                apiKey = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                requestBody = requestBody
            )
            // Gelen cevabın içindeki metni parse et
            val content = response
                .getAsJsonArray("choices")[0]
                .asJsonObject.getAsJsonObject("message")
                .get("content").asString

            return content

        } catch (e: Exception) {
            // Hata durumunda anlamlı bir mesaj döndür
            throw Exception("Failed to get chat response from OpenAI: ${e.message}")
        }
    }

    fun close() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
    }
}
