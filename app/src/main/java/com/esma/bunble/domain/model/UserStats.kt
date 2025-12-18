package com.esma.bunble.domain.model

data class UserStats(
    val streak: Int = 0,
    val learnedWords: Int = 0,
    val totalTimeSpentMinutes: Long = 0,
    val dailyGoalProgress: Int = 0,
    val dailyGoalTotal: Int = 100
)
