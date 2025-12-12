package com.esma.bunble.presentation.viewmodel.story

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.local.UserPreferencesRepository
import com.esma.bunble.domain.repository.IStoryRepository
import com.esma.bunble.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoriesViewModel @Inject constructor(
    private val storyRepository: IStoryRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = mutableStateOf(StoriesState())
    val state: State<StoriesState> = _state

    init {
        loadAllStories()
    }

    private fun loadAllStories() {
        viewModelScope.launch {
            val targetLang = userPrefs.targetLanguage.first()
            if (targetLang == null) {
                _state.value = state.value.copy(
                    error = "Please select your languages first",
                    isLoading = false
                )
                return@launch
            }

            storyRepository.getPublicStories(targetLang).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            publicStories = result.data ?: emptyList(),
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _state.value = state.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = state.value.copy(isLoading = true)
                    }
                }
            }.launchIn(viewModelScope)

            storyRepository.getUserStories().onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            userStories = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        _state.value = state.value.copy(error = result.message)
                    }
                    is Resource.Loading -> {  }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun deleteUserStory(storyId: String) {
        viewModelScope.launch {
            when (storyRepository.deleteUserStory(storyId)) {
                is Resource.Success -> {
                    _state.value = state.value.copy(
                        userStories = state.value.userStories.filterNot { it.id == storyId }
                    )
                }
                is Resource.Error -> { }
                is Resource.Loading -> { }
            }
        }
    }

    fun addUserStory(title: String, content: String) {
        viewModelScope.launch {
            _state.value = state.value.copy(
                isAddingStory = true,
                addStoryError = null,
                addStorySuccess = false
            )
            // KULLANICININ HEDEF DİLİNİ AL
            val targetLang = userPrefs.targetLanguage.first()
            if (targetLang.isNullOrBlank()) {
                _state.value = state.value.copy(
                    isAddingStory = false,
                    addStoryError = "Your target language is not set."
                )
                return@launch
            }

            // YAZILAN İÇERİĞİN DİLİNİ TESPİT ET
            val detectedLang = storyRepository.detectLanguage(content)
            if (detectedLang == null) {
                _state.value = state.value.copy(
                    isAddingStory = false,
                    addStoryError = "Could not detect the language. Please write more."
                )
                return@launch
            }

            // DİLLERİ KARŞILAŞTIR
            if (detectedLang != targetLang) {
                _state.value = state.value.copy(
                    isAddingStory = false,
                    addStoryError = "Please write your story in your target language, ${targetLang.uppercase()}..We detected it as ${detectedLang.uppercase()}."
                )
                return@launch
            }

            when (val result = storyRepository.addUserStory(title, content)) {
                is Resource.Success -> {
                    _state.value = state.value.copy(
                        isAddingStory = false,
                        addStorySuccess = true
                    )
                    refreshStories()
                }
                is Resource.Error -> {
                    _state.value = state.value.copy(
                        isAddingStory = false,
                        addStoryError = result.message
                    )
                }
                is Resource.Loading -> {  }
            }
        }
    }

    fun resetAddStoryState() {
        _state.value = state.value.copy(
            isAddingStory = false,
            addStoryError = null,
            addStorySuccess = false
        )
    }



    fun onTabSelected(index: Int) {
        _state.value = state.value.copy(selectedTabIndex = index)
    }

    // Dilleri değiştirme durumunda hikayeleri yeniden yükle
    fun refreshStories() {
        loadAllStories()

    }
}