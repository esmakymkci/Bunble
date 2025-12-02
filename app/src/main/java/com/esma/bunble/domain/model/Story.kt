package com.esma.bunble.domain.model

data class Story(
    val id: String,
    val title: String,
    val difficulty: String,
    val imageUrl: String
)

data class StoryDetail(
    val id: String,
    val title: String,
    val imageUrl: String,
    val content: String
)
