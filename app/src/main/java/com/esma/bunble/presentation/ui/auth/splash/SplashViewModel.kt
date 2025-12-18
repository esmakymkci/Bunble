package com.esma.bunble.presentation.ui.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.local.UserPreferencesRepository
import com.esma.bunble.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth // <-- 1. Firebase Auth'u import et
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject



@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPrefsRepo: UserPreferencesRepository,
    private val firebaseAuth: FirebaseAuth,
    private val userRepository : IUserRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<StartDestination?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val isFirstLaunch = userPrefsRepo.isFirstLaunch.first()

            if (isFirstLaunch) {
                _startDestination.value = StartDestination.LanguageSelection
            } else {
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    // Kullanıcı hiç yok, giriş ekranına yönlendir.
                    _startDestination.value = StartDestination.Authentication
                } else {
                    // Kullanıcı var, ama token'ı hala geçerli mi? Kontrol et.
                    try {
                        currentUser.getIdToken(true).await() // Token'ı yenilemeye zorla
                        // Oturum geçerli olduğu için streak güncelleme fonksiyonunu çağırıyoruz.
                        userRepository.updateUserStreak(currentUser.uid)
                        // Başarılı olursa: Token geçerli veya yenilendi. Ana ekrana git.
                        _startDestination.value = StartDestination.Home
                    } catch (e: Exception) {
                        // Başarısız olursa: Oturum geçersiz (şifre değişmiş, kullanıcı silinmiş vb.)
                        // Güvenli çıkış yap ve giriş ekranına yönlendir.
                        firebaseAuth.signOut()
                        _startDestination.value = StartDestination.Authentication
                    }
                }
            }
        }
    }
}
