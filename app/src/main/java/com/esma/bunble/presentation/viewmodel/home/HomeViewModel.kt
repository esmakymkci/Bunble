package com.esma.bunble.presentation.viewmodel.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.domain.model.LearningCategory
import com.esma.bunble.domain.repository.ILearningRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // <-- 1. Firestore'u import et
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // <-- Bu import gerekli
import javax.inject.Inject

// Data class yapısı doğru, aynen kalıyor
data class HomeScreenState(
    val categories: List<LearningCategory> = emptyList(),
    val isLoading: Boolean = false,
    val userName: String = "",
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ILearningRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore // <-- 2. Firestore'u enjekte et (userPrefsRepo'ya artık gerek yok)
) : ViewModel() {

    private val _state = mutableStateOf(HomeScreenState())
    val state: State<HomeScreenState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                // Kullanıcı giriş yapmamışsa, state'i güncelle ve çık
                _state.value = _state.value.copy(isLoading = false, error = "User not logged in.")
                return@launch
            }

            try {
                // --- ANA DEĞİŞİKLİK BURADA ---

                // 3. Kullanıcının profil dökümanını Firestore'dan tek seferde al
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()

                // 4. Firestore'dan hem kullanıcı adını hem de dil yolunu al
                val userNameFromFirestore = userDoc.getString("displayName")?.split(" ")?.firstOrNull() ?: "User"
                val languagePath = userDoc.getString("languagePath")

                // --- DEĞİŞİKLİK BİTTİ ---


                if (languagePath != null) {
                    // 5. Repository'i Firestore'dan gelen dil yolu ile çağır
                    val categoriesResult = repository.getCategories(languagePath)

                    // 6. State'i Firestore'dan gelen verilerle güncelle
                    _state.value = _state.value.copy(
                        userName = userNameFromFirestore,
                        categories = categoriesResult,
                        isLoading = false
                    )
                } else {
                    // Dil yolu bulunamazsa hata durumunu belirt
                    _state.value = _state.value.copy(
                        userName = userNameFromFirestore, // Dil yolu olmasa bile kullanıcı adını göster
                        isLoading = false,
                        error = "Language path not found. Please re-select your language."
                    )
                }
            } catch (e: Exception) {
                // Firestore veya repository'den veri alırken hata olursa
                _state.value = _state.value.copy(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}
