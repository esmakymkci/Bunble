package com.esma.bunble.presentation.viewmodel.home

import androidx.annotation.StringRes
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.R
import com.esma.bunble.domain.model.LearningCategory
import com.esma.bunble.domain.repository.ILearningRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ILearningRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = mutableStateOf(HomeScreenState())
    val state: State<HomeScreenState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                _state.value = _state.value.copy(isLoading = false, error = R.string.error_user_not_logged_in)
                return@launch
            }

            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()

                val userNameFromFirestore = userDoc.getString("displayName")?.split(" ")?.firstOrNull() ?: "User"
                val languagePath = userDoc.getString("languagePath")



                if (languagePath != null) {
                    val categoriesResult = repository.getCategories(languagePath)

                    _state.value = _state.value.copy(
                        userName = userNameFromFirestore,
                        categories = categoriesResult,
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        userName = userNameFromFirestore, // Dil yolu olmasa bile kullanıcı adını göster
                        isLoading = false,
                        error = R.string.error_language_path_not_found
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = R.string.error_unknown)
            }
        }
    }
}
