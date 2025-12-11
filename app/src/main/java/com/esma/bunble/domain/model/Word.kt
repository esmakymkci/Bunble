package com.esma.bunble.domain.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Bir listenin içindeki her bir kelime kartını temsil eder.
data class Word(
    val id: String = "",
    val sourceText: String = "",
    val translatedText: String = "",
    val phonetic: String? = null,
    val examples: List<String> = emptyList(),
    var isLearned: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null
)