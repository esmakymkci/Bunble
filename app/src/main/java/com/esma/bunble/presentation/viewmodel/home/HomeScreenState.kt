package com.esma.bunble.presentation.viewmodel.home

import androidx.annotation.StringRes
import com.esma.bunble.domain.model.LearningCategory

data class HomeScreenState(
    val categories: List<LearningCategory> = emptyList(),
    val isLoading: Boolean = false,
    val userName: String = "",
    @StringRes val error: Int? = null
)
