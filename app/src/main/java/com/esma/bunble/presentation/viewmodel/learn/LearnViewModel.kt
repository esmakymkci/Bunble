package com.esma.bunble.presentation.viewmodel.learn


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.domain.model.LearnItem
import com.esma.bunble.domain.repository.ILearningRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.media.MediaPlayer

// State class'ını mevcut öğeyi ve ilerlemeyi de içerecek şekilde güncelleyelim
data class LearningScreenState(
    val items: List<LearnItem> = emptyList(),
    val currentItemIndex: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    // Mevcut öğrenme kartını kolayca almak için bir yardımcı özellik
    val currentItem: LearnItem?
        get() = items.getOrNull(currentItemIndex)

    // Öğrenme sürecinin bitip bitmediğini kontrol etmek için bir yardımcı özellik
    val isFinished: Boolean
        get() = currentItemIndex >= items.size - 1 && items.isNotEmpty()
}

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val repository: ILearningRepository,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(LearningScreenState())
    val state: State<LearningScreenState> = _state
    private var mediaPlayer: MediaPlayer? = null

    init {
        // Navigasyondan gelen argümanları al
        val categoryId: String? = savedStateHandle.get("categoryId")
        // "phrases" mi "words" mü olduğunu belirleyen type argümanını al
        val type: String? = savedStateHandle.get("type")

        if (categoryId != null) {
            loadItems(categoryId, type)
        } else {
            _state.value = LearningScreenState(isLoading = false, error = "Category ID not found.")
        }
    }

    private fun loadItems(categoryId: String, type: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Kullanıcı profilinden dil yolunu dinamik olarak al
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                _state.value = LearningScreenState(isLoading = false, error = "User not logged in.")
                return@launch
            }

            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val languagePath = userDoc.getString("languagePath")

                if (languagePath != null) {
                    // "type" argümanına göre ya kelimeleri ya da ifadeleri çek
                    val itemsResult = when (type) {
                        "words" -> repository.getWords(languagePath, categoryId)
                        else -> repository.getPhrases(languagePath, categoryId) // Varsayılan olarak ifadeleri al
                    }
                    _state.value = LearningScreenState(items = itemsResult, isLoading = false)
                } else {
                    _state.value = LearningScreenState(isLoading = false, error = "Language path not found.")
                }
            } catch (e: Exception) {
                _state.value = LearningScreenState(isLoading = false, error = e.localizedMessage)
            }
        }
    }

    // "Continue" butonuna basıldığında bir sonraki öğeye geç
    fun onContinueClicked() {
        val currentState = _state.value
        if (!currentState.isFinished) {
            _state.value = currentState.copy(currentItemIndex = currentState.currentItemIndex + 1)
        }
    }

    fun playAudio(audioUrl: String?) {
        if (audioUrl.isNullOrBlank()) {
            // Ses URL'si boşsa hiçbir şey yapma
            return
        }

        // Eğer başka bir ses çalıyorsa, önce onu durdur ve kaynakları serbest bırak
        mediaPlayer?.release()
        mediaPlayer = null

        viewModelScope.launch {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioUrl) // 3. İnternetten ses dosyasını hazırla
                    prepareAsync() // 4. Asenkron olarak hazırla (UI'ı bloklamaz)
                    setOnPreparedListener { mp ->
                        mp.start() // 5. Hazır olduğunda çalmaya başla
                    }
                    setOnCompletionListener { mp ->
                        mp.release() // 6. Ses bittiğinde kaynakları serbest bırak
                        mediaPlayer = null
                    }
                    setOnErrorListener { mp, _, _ ->
                        mp.release()
                        mediaPlayer = null
                        true // Hatayı işlediğimizi belirtir
                    }
                }
            } catch (e: Exception) {
                // Hata olursa (örn. geçersiz URL), kaynakları serbest bırak
                mediaPlayer?.release()
                mediaPlayer = null
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release() // 7. ViewModel ölürken MediaPlayer'ı temizle
        mediaPlayer = null
    }
}
