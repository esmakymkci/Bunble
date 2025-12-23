package com.esma.bunble.presentation.viewmodel.chat

import com.google.mlkit.nl.translate.TranslateLanguage

data class Language(val code: String, val displayName: String)
data class ConversationState(
    val recognizedText: String = "",
    val translatedText: String = "",
    val isTranslating: Boolean = false,
    val isListening: Boolean = false,

    val sourceLanguage: Language = Language(TranslateLanguage.ENGLISH, "English"),
    val targetLanguage: Language = Language(TranslateLanguage.SPANISH, "Spanish"),

    // Dropdown menülerin açık/kapalı durumunu yönetmek için
    val isSourceMenuVisible: Boolean = false,
    val isTargetMenuVisible: Boolean = false,
    val error: String? = null,

    // Desteklenen dillerin tam listesi
    val availableLanguages: List<Language> = listOf(
        Language(TranslateLanguage.ENGLISH, "English"),
        Language(TranslateLanguage.SPANISH, "Spanish"),
        Language(TranslateLanguage.TURKISH, "Turkish"),
        Language(TranslateLanguage.FRENCH, "French"),
        Language(TranslateLanguage.GERMAN, "German")

    )
)