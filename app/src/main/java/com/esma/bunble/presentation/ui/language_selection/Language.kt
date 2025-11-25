package com.esma.bunble.presentation.ui.language_selection

data class Language(
    val code: String,       // "de"
    val name: String,       // "German"
    val flagEmoji: String   // "🇩🇪"
)
// Desteklediğimiz ve öğreteceğimiz dillerin listesi
val supportedLanguages = listOf(
    Language("es", "Spanish", "🇪🇸"),
    Language("fr", "French", "🇫🇷"),
    Language("de", "German", "🇩🇪"),
    Language("tr", "Turkish", "🇹🇷")
)