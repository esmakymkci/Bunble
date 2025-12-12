package com.esma.bunble.presentation.viewmodel.story

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.domain.model.UserStory
import com.esma.bunble.domain.repository.IStoryRepository
import com.esma.bunble.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// State'i tanımla
data class EditStoryState(
    val story: UserStory? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditStoryViewModel @Inject constructor(
    private val storyRepository: IStoryRepository,
    savedStateHandle: SavedStateHandle // storyId'yi rotadan almak için
) : ViewModel() {

    private val _state = mutableStateOf(EditStoryState())
    val state: androidx.compose.runtime.State<EditStoryState> = _state

    private val storyId: String = checkNotNull(savedStateHandle["storyId"])

    init {
        loadStory()
    }

    private fun loadStory() {
        viewModelScope.launch {
            _state.value = EditStoryState(isLoading = true)
            when (val result = storyRepository.getUserStory(storyId)) {
                is Resource.Success -> {
                    _state.value = EditStoryState(story = result.data)
                }
                is Resource.Error -> {
                    _state.value = EditStoryState(error = result.message)
                }
                else -> {}
            }
        }
    }

    fun updateStory(title: String, content: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, error = null)
            when (storyRepository.updateUserStory(storyId, title, content)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(isUpdating = false, updateSuccess = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isUpdating = false, error = "Failed to update story.")
                }
                else -> {}
            }
        }
    }
}
