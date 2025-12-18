package com.esma.bunble.domain.repository

import com.esma.bunble.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getUserStats(userId: String): Flow<UserStats>
}