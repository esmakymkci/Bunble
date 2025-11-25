package com.esma.bunble.presentation.viewmodel.category_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.domain.model.LearningCategory
import com.esma.bunble.domain.repository.ILearningRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class CategoryDetailState(
    val category: LearningCategory? = null, // State'i tek bir modelle yönetelim
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val repository: ILearningRepository, // Firestore yerine Repository
    private val firestore: FirebaseFirestore, // Kullanıcı verisi için hala gerekli
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(CategoryDetailState())
    val state: State<CategoryDetailState> = _state

    // Category ID'yi al, bu kısım doğru
    private val categoryId: String? = savedStateHandle.get("categoryId")

    init {
        if (categoryId != null) {
            loadCategoryDetails(categoryId)
        } else {
            // ID yoksa, hata durumu oluştur
            _state.value = CategoryDetailState(isLoading = false, error = "Category ID not found.")
        }
    }

    private fun loadCategoryDetails(categoryId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // En iyi pratik: Kullanıcının dil tercihini Firebase profilinden oku
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                _state.value = CategoryDetailState(isLoading = false, error = "User not logged in.")
                return@launch
            }

            try {
                // 1. Kullanıcının dil yolunu Firestore'dan al
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val languagePath = userDoc.getString("languagePath")

                if (languagePath != null) {
                    // 2. Repository'i dinamik dil yolu ve kategori ID'si ile çağır
                    val categoryDetails = repository.getCategoryDetails(languagePath, categoryId)
                    _state.value = CategoryDetailState(
                        isLoading = false,
                        category = categoryDetails,
                        error = if (categoryDetails == null) "Category not found." else null
                    )
                } else {
                    _state.value = CategoryDetailState(isLoading = false, error = "Language path not found in user profile.")
                }
            } catch (e: Exception) {
                _state.value = CategoryDetailState(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}
