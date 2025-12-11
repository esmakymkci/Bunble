package com.esma.bunble.presentation.viewmodel.story

import androidx.compose.ui.geometry.Offset
import com.esma.bunble.domain.model.StoryDetail

data class StoryDetailState(
    val storyDetail: StoryDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedWord: String = "",
    val translatedWord: String = "",
    val isSheetVisible: Boolean = false,
    val wordPosition: Offset = Offset.Zero
)
