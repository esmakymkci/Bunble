package com.esma.bunble.presentation.viewmodel.category_detail

import androidx.annotation.StringRes
import com.esma.bunble.domain.model.LearningCategory

data class CategoryDetailState(
    val category: LearningCategory? = null,
    val isLoading: Boolean = true,
    @StringRes val error: Int? = null
)
