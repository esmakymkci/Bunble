package com.esma.bunble.presentation.viewmodel.story

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.local.UserPreferencesRepository
import com.esma.bunble.domain.model.Story
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class StoriesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val _state = mutableStateOf(StoriesState())
    val state: State<StoriesState> = _state

    init {
        loadStories()
    }

    private fun loadStories() {
        viewModelScope.launch {
            _state.value = StoriesState(isLoading = true)

            try {
                // Flow'lardan değerleri al (tek seferlik)
                val sourceLang = userPrefs.sourceLanguage.first()
                val targetLang = userPrefs.targetLanguage.first()

                Log.d("StoriesViewModel", "Source: $sourceLang, Target: $targetLang")

                if (sourceLang == null || targetLang == null) {
                    Log.e("StoriesViewModel", "Language settings not found")
                    _state.value = StoriesState(
                        isLoading = false,
                        error = "Please select your languages first"
                    )
                    return@launch
                }

                // Firestore'dan hikayeleri çek
                Log.d("StoriesViewModel", "Fetching stories for language: $targetLang")

                val snapshot = firestore.collection("stories")
                    .whereArrayContains("available_languages", targetLang)
                    .get()
                    .await()

                Log.d("StoriesViewModel", "Found ${snapshot.size()} documents")

                val stories = snapshot.documents.mapNotNull { doc ->
                    try {
                        val titleKey = "title_$targetLang"
                        val title = doc.getString(titleKey)
                        val difficulty = doc.getString("difficulty")
                        val imageUrl = doc.getString("imageUrl")

                        Log.d("StoriesViewModel", "Story: ${doc.id}, title: $title, difficulty: $difficulty")

                        Story(
                            id = doc.id,
                            title = title ?: "No title",
                            difficulty = difficulty ?: "Beginner",
                            imageUrl = imageUrl ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("StoriesViewModel", "Error parsing story ${doc.id}: ${e.message}")
                        null
                    }
                }

                Log.d("StoriesViewModel", "Successfully parsed ${stories.size} stories")
                _state.value = StoriesState(stories = stories, isLoading = false)

            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error loading stories: ${e.message}", e)
                _state.value = StoriesState(
                    isLoading = false,
                    error = "Failed to load stories: ${e.localizedMessage}"
                )
            }
        }
    }

    // Dilleri değiştirme durumunda hikayeleri yeniden yükle
    fun refreshStories() {
        loadStories()
    }
}