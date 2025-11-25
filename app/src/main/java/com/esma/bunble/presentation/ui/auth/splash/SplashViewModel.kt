package com.esma.bunble.presentation.ui.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.local.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth // <-- 1. Firebase Auth'u import et
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// 2. Yeni bir hedef ekle: Home
sealed class StartDestination {
    object LanguageSelection : StartDestination()
    object Authentication : StartDestination()
    object Home : StartDestination() // <-- YENİ
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPrefsRepo: UserPreferencesRepository,
    private val firebaseAuth: FirebaseAuth // <-- 3. FirebaseAuth'u enjekte et
) : ViewModel() {

    private val _startDestination = MutableStateFlow<StartDestination?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            // DataStore'dan ilk açılış durumunu SADECE BİR KEZ oku
            val isFirstLaunch = userPrefsRepo.isFirstLaunch.first()

            if (isFirstLaunch) {
                // Her zaman olduğu gibi, ilk açılışsa dil seçimine yönlendir.
                _startDestination.value = StartDestination.LanguageSelection
            } else {
                // 4. İlk açılış değilse, giriş durumunu kontrol et.
                if (firebaseAuth.currentUser != null) {
                    // Kullanıcı zaten giriş yapmışsa, DOĞRUDAN ana ekrana yönlendir.
                    _startDestination.value = StartDestination.Home
                } else {
                    // Kullanıcı giriş yapmamışsa, kimlik doğrulama ekranına yönlendir.
                    _startDestination.value = StartDestination.Authentication
                }
            }
        }
    }
}
