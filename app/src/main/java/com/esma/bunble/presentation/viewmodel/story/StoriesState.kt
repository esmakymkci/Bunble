package com.esma.bunble.presentation.viewmodel.story

import com.esma.bunble.domain.model.Story

data class StoriesState(
    val stories: List<Story> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)