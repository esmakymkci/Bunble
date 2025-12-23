package com.esma.bunble.presentation.viewmodel.home

import androidx.annotation.StringRes
import com.esma.bunble.domain.model.LearningCategory

data class HomeScreenState(
    val categories: List<LearningCategory> = emptyList(),
    val isLoading: Boolean = false,
    val userName: String = "",
    @StringRes val error: Int? = null,
    val streak: Int = 0,
    val totalTimeSpentMinutes: Long = 0,
    val learnedWords: Int = 0,
    val level: Int = 1,
    val totalXp: Int = 750
)
