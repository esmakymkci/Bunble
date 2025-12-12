package com.esma.bunble.presentation.viewmodel.story

import com.esma.bunble.domain.model.PublicStory
import com.esma.bunble.domain.model.UserStory


data class StoriesState(
    val publicStories: List<PublicStory> = emptyList(),
    val userStories: List<UserStory> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isAddingStory: Boolean = false,
    val addStoryError: String? = null,
    val addStorySuccess: Boolean = false,
    val selectedTabIndex: Int = 0 // 0: Stories, 1: My Stories
)