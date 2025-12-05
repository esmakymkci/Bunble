package com.esma.bunble.presentation.viewmodel.my_lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.domain.model.WordList
import com.esma.bunble.domain.repository.IWordListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordListsViewModel @Inject constructor(
    private val repository: IWordListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordListsUiState())
    val uiState: StateFlow<WordListsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getAllLists()
                .catch { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.localizedMessage)
                    }
                }
                .collect { lists ->
                    // Veri her değiştiğinde burası çalışacak
                    _uiState.update {
                        it.copy(isLoading = false, lists = lists)
                    }
                }
        }
    }


    fun createList(title: String, sourceLang: String, targetLang: String) {
        viewModelScope.launch {
            try {
                repository.createList(title, sourceLang, targetLang)
            } catch (e: Exception) {
                // Hata durumu (örneğin bir snackbar göster)
                _uiState.update { it.copy(error = "Failed to create list: ${e.localizedMessage}") }
            }
        }
    }
}

// ViewModel'in durumunu (state) temsil eden bir data class
data class WordListsUiState(
    val isLoading: Boolean = false,
    val lists: List<WordList> = emptyList(),
    val error: String? = null
)
