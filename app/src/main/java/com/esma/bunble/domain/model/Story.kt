package com.esma.bunble.domain.model

// "hikaye listesi" kartları bu arayüzü temel alacak.
sealed interface StoryListItem {
    val id: String
    val title: String
    val imageUrl: String
}

data class PublicStory(
    override val id: String = "",
    override val title: String = "",
    val difficulty: String = "",
    override val imageUrl: String = ""
) : StoryListItem

data class UserStory(
    override val id: String = "",
    override val title: String = "",
    val content: String = "",
    override val imageUrl: String = ""
) : StoryListItem

data class StoryDetail(
    val id: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val content: String = "",
    val isUserStory: Boolean = false,
    val authorId: String? = null
)
