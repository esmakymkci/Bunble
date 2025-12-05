package com.esma.bunble.presentation.viewmodel.my_lists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.remote.openai.OpenAIRepository
import com.esma.bunble.domain.model.Word
import com.esma.bunble.domain.model.WordList
import com.esma.bunble.domain.repository.IWordListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ListDetailUiState(
    val listDetails: WordList? = null,
    val words: List<Word> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isTranslating: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val wordListRepository: IWordListRepository,
    private val openAIRepository: OpenAIRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val listId: StateFlow<String> = savedStateHandle.getStateFlow("listId", "")

    // Değiştirilebilir (Mutable) bir state flow oluşturuyoruz.
    private val _uiState = MutableStateFlow(ListDetailUiState(isLoading = true))
    // UI'ın dinleyeceği, değiştirilemez (immutable) state flow.
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

    init {
        // ViewModel başladığında, listId'yi dinlemeye başla ve veri akışlarını birleştir.
        viewModelScope.launch {
            listId.flatMapLatest { id ->
                if (id.isBlank()) {
                    flowOf(Pair<WordList?, List<Word>>(null, emptyList()))
                } else {
                    combine(
                        wordListRepository.getListDetails(id),
                        wordListRepository.getWordsForList(id)
                    ) { details, words ->
                        Pair(details, words)
                    }
                }
            }.collect { (details, words) ->
                // Veritabanından gelen her yeni veriyle _uiState'i güncelle.
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        listDetails = details,
                        words = words,
                        error = if (details == null && listId.value.isNotBlank()) "List not found" else null
                    )
                }
            }
        }
    }


    fun deleteWord(word: Word) {
        val currentListId = listId.value
        if (currentListId.isBlank()) return

        viewModelScope.launch {
            wordListRepository.deleteWord(currentListId, word.id, word.isLearned)
        }
    }

    fun updateWordLearnedStatus(word: Word) {
        val currentListId = listId.value
        if (currentListId.isBlank()) return

        viewModelScope.launch {
            val updatedWord = word.copy(isLearned = !word.isLearned)
            wordListRepository.updateWord(currentListId, updatedWord)
        }
    }


    fun addWord(sourceText: String) {
        // Mevcut listeyi al (dillere erişmek için)
        val list = _uiState.value.listDetails ?: return
        val currentListId = listId.value

        if (sourceText.isBlank() || currentListId.isBlank()) return

        viewModelScope.launch {
            // İşlem başlarken yüklenme durumunu 'true' yap
            _uiState.update { it.copy(isTranslating = true, error = null) }

            try {
                // OpenAI'den çeviri ve detayları al
                val detailedWord = openAIRepository.getWordDetails(
                    sourceText = sourceText,
                    sourceLang = list.sourceLang,
                    targetLang = list.targetLang
                )
                // Veritabanına ekle (Bu işlem bittiğinde, `init` bloğundaki `collect` otomatik olarak tetiklenir)
                wordListRepository.addWordToList(currentListId, detailedWord)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Translation Error: ${e.message}") }
            } finally {
                // İşlem bittiğinde (başarılı ya da hatalı) yüklenme durumunu 'false' yap
                _uiState.update { it.copy(isTranslating = false) }
            }
        }
    }
}
