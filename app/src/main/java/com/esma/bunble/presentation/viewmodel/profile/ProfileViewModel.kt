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

                // Kullanıcı adını al, eğer null veya boş ise "User" kullan.
                val username = if (userStats.displayName.isNullOrBlank()) "User" else userStats.displayName

                // Dil yolunu ("tr-de") alıp "🇹🇷 → 🇩🇪" formatına çevir.
                val languageDescription = formatLanguagePath(userStats.languagePath)


                _state.update { currentState ->
                    currentState.copy(
                        userName = username,
                        email = currentUser.email ?: "",
                        isUserLoggedIn = true,
                        currentLanguage = languageDescription,
                        statistics = dynamicStatistics,
                        isLoading = false
                    )
                }
            }
            .catch { exception ->
                _state.update { it.copy(isLoading = false, isUserLoggedIn = false) }
            }
            .launchIn(viewModelScope)
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
        auth.signOut()
        // State'i güncelle. Artık bir kullanıcı yok.
        _state.update { it.copy(isUserLoggedIn = false, isLoading = false, email = "", statistics = emptyList()) }
        viewModelScope.launch {
            _navigationEvent.send(ProfileNavigationEvent.NavigateToSignIn)
        }
    }

}
