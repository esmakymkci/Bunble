package com.esma.bunble.presentation.viewmodel.story

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.local.UserPreferencesRepository
import com.esma.bunble.domain.model.StoryDetail
import com.esma.bunble.domain.repository.IStoryRepository
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
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

    val currentSourceLanguage = mutableStateOf<String?>(null)
    val currentTargetLanguage = mutableStateOf<String?>(null)


    init {
        val storyId: String? = savedStateHandle.get("storyId")
        if (storyId == null) {
            _state.value = StoryDetailState(isLoading = false, error = "Story ID not found")
        } else {
            loadStoryDetail(storyId)
        }
    }

    private fun loadStoryDetail(storyId: String) {
        viewModelScope.launch {
            _state.value = StoryDetailState(isLoading = true)
            // Dil tercihlerini dinle
            combine(userPrefs.sourceLanguage, userPrefs.targetLanguage) { sourceLang, targetLang ->
                currentSourceLanguage.value = sourceLang // ViewModel'da dili sakla
                currentTargetLanguage.value = targetLang // ViewModel'da dili sakla
                Pair(sourceLang, targetLang)
            }.collect { (sourceLang, targetLang) ->
                if (sourceLang == null || targetLang == null) {
                    _state.value = StoryDetailState(isLoading = false, error = "Language settings not found.")
                    return@collect
                }

                try {
                    val docs = storyRepository.getStoryDetail(storyId)
                    val storyDoc = docs["story"] as com.google.firebase.firestore.DocumentSnapshot
                    val contentDoc = docs["content"] as com.google.firebase.firestore.DocumentSnapshot

                    val titleKey = "title_$targetLang"
                    val contentKey = "content.$targetLang" // Map içindeki alana erişim

                    val storyDetail = StoryDetail(
                        id = storyDoc.id,
                        title = storyDoc.getString(titleKey) ?: "Title not found",
                        imageUrl = storyDoc.getString("imageUrl") ?: "",
                        content = contentDoc.getString(contentKey) ?: "Content not available in this language."
                    )
                    _state.value = StoryDetailState(storyDetail = storyDetail, isLoading = false)
                } catch (e: Exception) {
                    _state.value = StoryDetailState(isLoading = false, error = e.localizedMessage)
                }
            }
        }
    }

    fun onWordClicked(word: String, position: Offset) {
        val sourceLang = currentTargetLanguage.value ?: return
        val targetLang = currentSourceLanguage.value ?: return

        _state.value = _state.value.copy(
            isSheetVisible = true,
            selectedWord = word,
            wordPosition = position,
            translatedWord = "Translating..."
        )

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded().addOnSuccessListener {
            translator.translate(word)
                .addOnSuccessListener { translation ->
                    _state.value = _state.value.copy(translatedWord = translation)
                }
                .addOnFailureListener {
                    _state.value = _state.value.copy(translatedWord = "Error")
                }
        }.addOnFailureListener {
            _state.value = _state.value.copy(translatedWord = "Model download failed")
        }
    }

    // onSheetDismiss fonksiyonu Popup'ı kapatmak için kullanılacak
    fun onSheetDismiss() {
        _state.value = _state.value.copy(
            isSheetVisible = false,
            selectedWord = "",
            translatedWord = "",
            // wordPosition = Offset.Zero
        )
    }
}

/*

combine: İki farklı akışı (kullanıcının kaynak ve hedef dil tercihleri) birleştirir.
Bu akışlardan herhangi biri değiştiğinde, combine bloğu yeniden çalışır.

collect: Bu birleştirilmiş akışı dinlemeye başlar. Dil ayarları geldiğinde, storyRepository aracılığıyla
Firebase'den hikaye verisini çeker, StoryDetail modeline dönüştürür ve _state.value'yu güncelleyerek UI'ın yeniden çizilmesini tetikler.



 */