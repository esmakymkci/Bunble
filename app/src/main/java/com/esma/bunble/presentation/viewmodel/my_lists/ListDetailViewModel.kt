package com.esma.bunble.presentation.viewmodel.my_lists

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import java.util.Locale
import androidx.lifecycle.SavedStateHandle
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ListDetailViewModel @Inject constructor(
    private val app: Application,
    private val wordListRepository: IWordListRepository,
    private val openAIRepository: OpenAIRepository,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app), TextToSpeech.OnInitListener {

    private val listId: StateFlow<String> = savedStateHandle.getStateFlow("listId", "")

    // Değiştirilebilir (Mutable) bir state flow oluşturuyoruz.
    private val _uiState = MutableStateFlow(ListDetailUiState(isLoading = true))
    // UI'ın dinleyeceği, değiştirilemez (immutable) state flow.
    val uiState: StateFlow<ListDetailUiState> = _uiState.asStateFlow()

    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(app, this)
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

    // TTS motoru başarıyla başlatıldığında bu fonksiyon tetiklenir.
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Başarılı, artık kullanıma hazır.
            Log.d("TTS", "Initialization Success")
        } else {
            _uiState.update { it.copy(error = "TTS could not be initialized.") }
        }
    }


    // UI'dan çağrılacak olan ana fonksiyonumuz
    fun speakWord(word: Word) {
        val targetLanguageCode = uiState.value.listDetails?.targetLang ?: return
        val locale = Locale(targetLanguageCode) // "en", "es", "fr" gibi kodlardan Locale oluştur

        // Cihaz bu dili destekliyor mu kontrol et
        val result = tts.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            _uiState.update { it.copy(error = "This language is not supported by TTS.") }
            return
        }

        // Kelimeyi seslendir
        tts.speak(word.translatedText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // ViewModel temizlendiğinde TTS motorunu kapat (hafıza sızıntısını önler)
    override fun onCleared() {
        super.onCleared()
        if (this::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
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
