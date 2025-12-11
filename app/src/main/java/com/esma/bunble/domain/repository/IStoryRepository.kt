package com.esma.bunble.domain.repository

import com.esma.bunble.domain.model.Story
import com.esma.bunble.domain.model.StoryDetail

interface IStoryRepository {
    suspend fun getStories(targetLanguage: String): List<Story>
    suspend fun getStoryDetail(storyId: String): Map<String, Any>
}
