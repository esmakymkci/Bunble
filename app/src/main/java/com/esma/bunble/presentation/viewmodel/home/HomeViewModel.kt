package com.esma.bunble.presentation.viewmodel.home

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.esma.bunble.R
import com.esma.bunble.domain.repository.ILearningRepository
import com.esma.bunble.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ILearningRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val imageLoader: ImageLoader,
    private val application: Application,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _state = mutableStateOf(HomeScreenState())
    val state: State<HomeScreenState> = _state

    init {
        loadData()
        loadUserStats()
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

                    //  Resimleri önceden yükle
                    val imageJobs = categoriesResult.map { category ->
                        // Her resim yüklemesini ayrı bir 'async' bloğuna al
                        async(Dispatchers.IO) {
                            val request = ImageRequest.Builder(application)
                                .data(category.imageUrl)
                                .memoryCachePolicy(CachePolicy.ENABLED) // Belleğe önbellekle
                                .diskCachePolicy(CachePolicy.ENABLED)   // Diske önbellekle
                                .build()
                            imageLoader.execute(request) // Yüklemenin bitmesini bekle
                        }
                    }
                    imageJobs.awaitAll()


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

    private fun loadUserStats() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            return
        }

        userRepository.getUserStats(userId)
            .onEach { userStats ->
                _state.value = _state.value.copy(
                    streak = userStats.streak,
                    totalTimeSpentMinutes = userStats.totalTimeSpentMinutes,
                    learnedWords = userStats.learnedWords
                )
            }
            .launchIn(viewModelScope)
    }
}
