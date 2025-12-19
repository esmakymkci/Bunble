package com.esma.bunble.domain.model

data class UserStats(
    // Temel Kullanıcı Bilgileri
    val uid: String = "",
    val displayName: String? = null,
    val email: String = "",
    val languagePath: String? = null,
    // İlerleme İstatistikleri
    val streak: Int = 0,
    val learnedWords: Int = 0,
    val totalTimeSpentMinutes: Long = 0,
    // Günlük Hedef
    val dailyGoalProgress: Int = 0,
    val dailyGoalTotal: Int = 100,
    // Level ve XP Sistemi
    val totalXp: Int = 0,
    val currentLevel: Int = 1
)
