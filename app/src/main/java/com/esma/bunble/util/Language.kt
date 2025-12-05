package com.esma.bunble.util

data class Language(
    val code: String,       // Örn: "en"
    val name: String,       // Örn: "English"
    val flagEmoji: String   // Örn: "🇺🇸"
)

// Desteklenen dillerin tam listesi
val supportedLanguages = listOf(
    Language("en", "English", "🇺🇸"),
    Language("tr", "Turkish", "🇹🇷"),
    Language("es", "Spanish", "🇪🇸"),
    Language("de", "German", "🇩🇪"),
    Language("fr", "French", "🇫🇷"),
    Language("it", "Italian", "🇮🇹")
// İhtiyacına göre daha fazla dil ekleyebilirsin
).distinctBy { it.code }.sortedBy { it.name } // Tekrarları sil ve alfabetik sırala

