package com.esma.bunble.presentation.viewmodel.my_lists

import com.esma.bunble.domain.model.WordList

// ViewModel'in durumunu (state) temsil eden bir data class
data class WordListsUiState(
    val isLoading: Boolean = false,
    val lists: List<WordList> = emptyList(),
    val error: String? = null
)
