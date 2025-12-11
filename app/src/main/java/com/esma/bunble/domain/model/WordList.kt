package com.esma.bunble.domain.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Ana ekranda gösterilecek liste özetini temsil eder.
data class WordList(
    val id: String = "",
    val title: String = "",

    @get:PropertyName("sourceLanguageCode")
    @set:PropertyName("sourceLanguageCode")
    var sourceLang: String = "",

    @get:PropertyName("targetLanguageCode")
    @set:PropertyName("targetLanguageCode")
    var targetLang: String = "",

    val wordCount: Int = 0,
    val learnedCount: Int = 0,
    @ServerTimestamp
    val createdAt: Date? = null
) {
    // Tamamlanma yüzdesini hesaplamak için bir yardımcı özellik.
    @get:PropertyName("completionPercentage")
    val completionPercentage: Int
        get() = if (wordCount > 0) (learnedCount * 100) / wordCount else 0
}