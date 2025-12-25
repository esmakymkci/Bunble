package com.esma.bunble.presentation.viewmodel.category_detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.R
import com.esma.bunble.domain.repository.ILearningRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val repository: ILearningRepository,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(CategoryDetailState())
    val state: State<CategoryDetailState> = _state

    private val categoryId: String? = savedStateHandle.get("categoryId")

    init {
        if (categoryId != null) {
            loadCategoryDetails(categoryId)
        } else {
            _state.value = CategoryDetailState(isLoading = false, error =  R.string.error_category_id_not_found)
        }
    }

    private fun loadCategoryDetails(categoryId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                _state.value = CategoryDetailState(isLoading = false, error = R.string.error_user_not_logged_in)
                return@launch
            }

            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val languagePath = userDoc.getString("languagePath")

                if (languagePath != null) {
                    val categoryDetails = repository.getCategoryDetails(languagePath, categoryId)
                    _state.value = CategoryDetailState(
                        isLoading = false,
                        category = categoryDetails,
                        error = if (categoryDetails == null) R.string.error_category_not_found else null                    )
                } else {
                    _state.value = CategoryDetailState(isLoading = false, error = R.string.error_language_path_not_found)
                }
            } catch (e: Exception) {
                _state.value = CategoryDetailState(isLoading = false, error = R.string.error_unknown)
            }
        }
    }
}

/*

savedStateHandle: SavedStateHandle: Navigasyon yoluyla gelen argümanları (categoryId gibi) güvenli bir şekilde yakalamak için kullanılır.
savedStateHandle.get("categoryId"): Navigasyon rotasındaki (.../{categoryId}) categoryId argümanını yakalar.

 */
