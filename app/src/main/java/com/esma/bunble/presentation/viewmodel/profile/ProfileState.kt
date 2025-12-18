package com.esma.bunble.presentation.viewmodel.profile

// İstatistik kartlarındaki verileri temsil eder
data class Statistic(
    val title: String,
    val value: String,
    val unit: String
)

// Profil ekranının anlık durumunu tutar
data class ProfileState(
    val userName: String = "Alex Johnson",
    val userEmail: String = "",
    val currentLanguage: String = "Learning Spanish",
    val level: Int = 12,
    val levelName: String = "Intermediate Scholar",
    val currentXp: Int = 750,
    val totalXp: Int = 1000,
    val statistics: List<Statistic> = emptyList(),
    val isDarkMode: Boolean = false,
    val isUserLoggedIn: Boolean = false,
    val email: String = "",
    val areNotificationsEnabled: Boolean = false,
    val isLoading: Boolean = true
)
