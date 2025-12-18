package com.esma.bunble.domain.repository

import com.esma.bunble.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getUserStats(userId: String): Flow<UserStats>
    suspend fun updateUserStreak(userId: String)
    suspend fun incrementTotalTimeSpent(userId: String, minutes: Long)
    suspend fun updateTotalLearnedWords(userId: String, amount: Int)
}