package com.esma.bunble.presentation.viewmodel.story

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.local.UserPreferencesRepository
import com.esma.bunble.domain.repository.IStoryRepository
import com.esma.bunble.util.Resource
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryDetailViewModel @Inject constructor(
    private val storyRepository: IStoryRepository,
    private val userPrefs: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(StoryDetailState())
    val state: androidx.compose.runtime.State<StoryDetailState> = _state

    private val storyId: String? = savedStateHandle["storyId"]

    init {
        if (storyId != null) {
            loadStoryDetail(storyId)
        } else {
            _state.value = StoryDetailState(error = "Story ID not found.")
        }
    }

    fun refreshStoryDetail() {
        if (storyId != null) {
            loadStoryDetail(storyId)
        }
    }


    private fun loadStoryDetail(storyId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = storyRepository.getStoryDetail(storyId)) {
                is Resource.Success -> {
                    _state.value = StoryDetailState(
                        storyDetail = result.data,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _state.value = StoryDetailState(
                        isLoading = false,
                        error = result.message ?: "An unknown error occurred"
                    )
                }
                is Resource.Loading -> { /* Handled */ }
            }
        }
    }

    // Hem kelime hem cümle için kullanılacak ortak çeviri fonksiyonu
    private fun translate(text: String, position: Offset) {
        viewModelScope.launch {
            val sourceLang = userPrefs.targetLanguage.first()
            val targetLang = userPrefs.sourceLanguage.first()

            if (sourceLang == null || targetLang == null) {
                _state.value = _state.value.copy(translatedWord = "Language preferences not set")
                return@launch
            }

            _state.value = _state.value.copy(
                isSheetVisible = true,
                selectedWord = text,
                wordPosition = position,
                translatedWord = "Translating..."
            )

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            val translator = Translation.getClient(options)
            translator.downloadModelIfNeeded().addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translation ->
                        _state.value = _state.value.copy(translatedWord = translation)
                    }
                    .addOnFailureListener {
                        _state.value = _state.value.copy(translatedWord = "Translation Error")
                    }
            }.addOnFailureListener {
                _state.value = _state.value.copy(translatedWord = "Model download failed")
            }
        }
    }

    fun onWordClicked(word: String, position: Offset) {
        translate(word, position)
    }

    fun onSentenceSelected(sentence: String, position: Offset) {
        translate(sentence, position)
    }

    fun onSheetDismiss() {
        _state.value = _state.value.copy(
            isSheetVisible = false,
            selectedWord = "",
            translatedWord = "",
        )
    }
}
