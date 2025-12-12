package com.esma.bunble.domain.repository

import com.esma.bunble.domain.model.PublicStory
import com.esma.bunble.domain.model.StoryDetail
import com.esma.bunble.domain.model.UserStory
import com.esma.bunble.util.Resource
import kotlinx.coroutines.flow.Flow

interface IStoryRepository {
    fun getPublicStories(targetLanguage: String): Flow<Resource<List<PublicStory>>>
    fun getUserStories(): Flow<Resource<List<UserStory>>>
    suspend fun getStoryDetail(storyId: String): Resource<StoryDetail>
    suspend fun deleteUserStory(storyId: String): Resource<Unit>
    suspend fun addUserStory(title: String, content: String): Resource<Unit>
    suspend fun detectLanguage(text: String): String?
    suspend fun getUserStory(storyId: String): Resource<UserStory>
    suspend fun updateUserStory(storyId: String, title: String, content: String): Resource<Unit>
}
