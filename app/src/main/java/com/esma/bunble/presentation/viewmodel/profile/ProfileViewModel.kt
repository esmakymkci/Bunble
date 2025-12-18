package com.esma.bunble.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.domain.model.UserStats
import com.esma.bunble.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


// Navigasyon olayını temsil eden bir sınıf
sealed class ProfileNavigationEvent {
    data object NavigateToSignIn : ProfileNavigationEvent()
}


@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _navigationEvent = Channel<ProfileNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()


    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _state.update { it.copy(isLoading = false, isUserLoggedIn = false) }
            return
        }

        _state.update { it.copy(email = currentUser.email ?: "", isUserLoggedIn = true) }

        // UserRepository'den gelen istatistik akışını dinlemeye başla.
        userRepository.getUserStats(currentUser.uid)
            .onEach { userStats ->
                // Firestore'dan her yeni veri geldiğinde bu blok çalışacak.
                val dynamicStatistics = mapUserStatsToUI(userStats)

                _state.update {
                    it.copy(
                        statistics = dynamicStatistics,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // Veri modelini UI'da gösterilecek listeye çeviren yardımcı fonksiyon
    private fun mapUserStatsToUI(userStats: UserStats): List<Statistic> {
        val totalHours = userStats.totalTimeSpentMinutes / 60
        val remainingMinutes = userStats.totalTimeSpentMinutes % 60
        val timeString = if (totalHours > 0) "${totalHours}h ${remainingMinutes}m" else "${remainingMinutes}m"

        return listOf(
            Statistic("STREAK", userStats.streak.toString(), "days"),
            Statistic("LEARNED", userStats.learnedWords.toString(), "words"),
            Statistic("TIME", timeString, "total"),
            Statistic("DAILY", "${userStats.dailyGoalProgress}/${userStats.dailyGoalTotal}", "day")
        )
    }


    fun onDarkModeChanged(isEnabled: Boolean) {
        _state.update { it.copy(isDarkMode = isEnabled) }
    }

    fun onNotificationsChanged(isEnabled: Boolean) {
        _state.update { it.copy(areNotificationsEnabled = isEnabled) }
    }

    fun onSignOutClicked() {
        // 1. Firebase'den çıkış yap
        auth.signOut()

        // 2. (İyileştirme) State'i güncelle. Artık bir kullanıcı yok.
        _state.update { it.copy(isUserLoggedIn = false, isLoading = false, email = "", statistics = emptyList()) }

        // 3. Yönlendirme olayını gönder
        viewModelScope.launch {
            _navigationEvent.send(ProfileNavigationEvent.NavigateToSignIn)
        }
    }

}
