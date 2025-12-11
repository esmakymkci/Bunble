package com.esma.bunble.presentation.viewmodel.my_lists

import com.esma.bunble.domain.model.Word
import com.esma.bunble.domain.model.WordList

data class ListDetailUiState(
    val listDetails: WordList? = null,
    val words: List<Word> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isTranslating: Boolean = false
)

