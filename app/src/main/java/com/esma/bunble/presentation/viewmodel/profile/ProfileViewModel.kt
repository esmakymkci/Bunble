package com.esma.bunble.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.domain.model.UserStats
import com.esma.bunble.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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

    private var userProfileJob: Job? = null


    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // Mevcut Job'u iptal et (eğer varsa, yeniden çağırmalara karşı önlem)
        userProfileJob?.cancel()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _state.update { it.copy(isLoading = false, isUserLoggedIn = false) }
            return
        }

        _state.update { it.copy(email = currentUser.email ?: "", isUserLoggedIn = true) }

        userProfileJob = userRepository.getUserStats(currentUser.uid)
            .onEach { userStats ->
                // Her yeni veri geldiğinde bu blok çalışır.
                val dynamicStatistics = mapUserStatsToUI(userStats)
                val username = if (userStats.displayName.isNullOrBlank()) "User" else userStats.displayName
                val languageDescription = formatLanguagePath(userStats.languagePath)

                _state.update { currentState ->
                    currentState.copy(
                        userName = username,
                        isUserLoggedIn = true,
                        currentLanguage = languageDescription,
                        statistics = dynamicStatistics,
                        isLoading = false
                    )
                }
            }
            .catch { exception ->
                // PERMISSION_DENIED gibi hataları burada yakala ve state'i güncelle.
                _state.update { it.copy(isLoading = false, error = exception.message, isUserLoggedIn = false) }
            }
            .launchIn(viewModelScope) // Flow'u viewModelScope'ta başlat ve Job'u al.
    }

    private fun getFlagEmojiForLanguage(code: String): String {
        return when (code.lowercase()) {
            "tr" -> "🇹🇷"
            "en" -> "🇬🇧"
            "de" -> "🇩🇪"
            "es" -> "🇪🇸"
            "fr" -> "🇫🇷"
            "it" -> "🇮🇹"
            else -> "🏳️"
        }
    }


    // "tr-en" gibi bir dil yolunu "🇹🇷 → 🇬🇧" formatına çevirir.
    private fun formatLanguagePath(languagePath: String?): String {
        if (languagePath.isNullOrBlank() || !languagePath.contains("-")) {
            return "Language Not Set"
        }
        val parts = languagePath.split("-")
        if (parts.size < 2) return "Invalid Format"

        val sourceFlag = getFlagEmojiForLanguage(parts[0])
        val targetFlag = getFlagEmojiForLanguage(parts[1])

        return "$sourceFlag → $targetFlag"
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
        userProfileJob?.cancel()
        userProfileJob = null
        userRepository.cleanupListeners()
        auth.signOut()
        // State'i güncelle. Artık bir kullanıcı yok.
        _state.update { it.copy(isUserLoggedIn = false, isLoading = false, email = "", userName = "",statistics = emptyList()) }
        viewModelScope.launch {
            _navigationEvent.send(ProfileNavigationEvent.NavigateToSignIn)
        }
    }

    override fun onCleared() {
        // ViewModel yok edilirken tüm kaynakları temizlediğimizden emin ol.
        userProfileJob?.cancel()
        userRepository.cleanupListeners()
        super.onCleared()
    }

}
