package com.esma.bunble.presentation.ui.language_selection

data class Language(
    val code: String,       // "de"
    val name: String,       // "German"
    val flagEmoji: String   // "🇩🇪"
)
val supportedLanguages = listOf(
    Language("es", "Spanish", "🇪🇸"),
    Language("es", "Spanish", "🇪🇸") ,
    Language("de", "German", "🇩🇪"),
    Language("tr", "Turkish", "🇹🇷")
)