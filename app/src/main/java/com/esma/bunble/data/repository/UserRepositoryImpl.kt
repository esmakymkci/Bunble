package com.esma.bunble.data.repository

import com.esma.bunble.domain.model.UserStats
import com.esma.bunble.domain.repository.IUserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : IUserRepository {

    override fun getUserStats(userId: String): Flow<UserStats> =
        callbackFlow {
            val userDocumentRef = firestore.collection("users").document(userId)
            val listener = userDocumentRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val stats = snapshot.toObject(UserStats::class.java)
                    if (stats != null) {
                        trySend(stats)
                    } else {
                        trySend(UserStats())
                    }
                } else {
                    trySend(UserStats())
                }
            }
            awaitClose { listener.remove() }
        }
}