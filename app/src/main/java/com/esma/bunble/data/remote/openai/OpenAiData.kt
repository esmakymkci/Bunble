package com.esma.bunble.data.remote.openai

// OpenAI'ye göndereceğimiz isteğin gövde (body) yapısı
data class OpenAIRequestBody(
    val model: String,
    val messages: List<Message>,
    val response_format: ResponseFormat
)

data class Message(
    val role: String,
    val content: String
)

data class ResponseFormat(
    val type: String
)
