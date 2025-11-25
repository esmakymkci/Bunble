package com.esma.bunble.domain.model

data class LearnItem(
    val text: String,
    val phonetic: String,
    val meaning: String,
    val translation: String,
    val imageUrl: String,
    val audioUrl: String? = null
)
