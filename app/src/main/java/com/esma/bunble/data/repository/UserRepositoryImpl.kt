package com.esma.bunble.data.repository

import com.esma.bunble.domain.model.UserStats
import com.esma.bunble.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IUserRepository {

    private val activeListeners = mutableListOf<ListenerRegistration>()

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
                    trySend(stats ?: UserStats())
                } else {
                    trySend(UserStats())
                }
            }

            activeListeners.add(listener)

            awaitClose {
                listener.remove()
                activeListeners.remove(listener)
            }
        }
    override fun cleanupListeners() {
        // Listedeki her bir dinleyiciyi güvenli bir şekilde kapatın.
        activeListeners.forEach { it.remove() }
        // Listeyi tamamen temizleyin.
        activeListeners.clear()
    }

    override suspend fun updateUserStreak(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)

                // Firestore'dan son giriş tarihini alıyoruz.
                // Not: Bu alanın Firestore'da 'lastLogin' adıyla ve Timestamp tipiyle olması gerekir.
                val lastLoginTimestamp = snapshot.getTimestamp("lastLogin")
                val today = Calendar.getInstance()

                if (lastLoginTimestamp == null) {
                    // İlk girişi, seri 1'den başlar.
                    transaction.update(userDocRef, "streak", 1)
                } else {
                    val lastLoginDate = Calendar.getInstance().apply {
                        time = lastLoginTimestamp.toDate()
                    }

                    val isSameDay = today.get(Calendar.YEAR) == lastLoginDate.get(Calendar.YEAR) &&
                            today.get(Calendar.DAY_OF_YEAR) == lastLoginDate.get(Calendar.DAY_OF_YEAR)

                    // Eğer bugün zaten giriş yapılmışsa, hiçbir şey yapma.
                    if (isSameDay) {
                        return@runTransaction null
                    }
                    // Son girişin dün olup olmadığını kontrol et.
                    lastLoginDate.add(Calendar.DAY_OF_YEAR, 1)
                    val wasYesterday = today.get(Calendar.YEAR) == lastLoginDate.get(Calendar.YEAR) &&
                            today.get(Calendar.DAY_OF_YEAR) == lastLoginDate.get(Calendar.DAY_OF_YEAR)

                    if (wasYesterday) {
                        // Seri devam ediyor, 1 artır.
                        transaction.update(userDocRef, "streak", FieldValue.increment(1))
                    } else {
                        // Seri bozulmuş, 1'e sıfırla.
                        transaction.update(userDocRef, "streak", 1)
                    }
                }

                // Her durumda son giriş tarihini bugünün tarihiyle güncelle.
                transaction.update(userDocRef, "lastLogin", FieldValue.serverTimestamp())
                null // runTransaction bir değer döndürmek zorunda, null döndürebiliriz.
            }.await()
        } catch (e: Exception) {
            // Hata durumunda loglama yapılabilir.
            println("Streak güncelleme hatası: ${e.message}")
        }
    }


    override suspend fun incrementTotalTimeSpent(userId: String, minutes: Long) {
        val userDocRef = firestore.collection("users").document(userId)
        try {
            // 'totalTimeSpentMinutes' alanını belirtilen dakika kadar artır.
            userDocRef.update("totalTimeSpentMinutes", FieldValue.increment(minutes)).await()
        } catch (e: Exception) {
            println("Süre güncelleme hatası: ${e.message}")
        }
    }

    override suspend fun updateTotalLearnedWords(userId: String, amount: Int) {
        val userDocRef = firestore.collection("users").document(userId)
        try {
            // 'UserStats' modelindeki 'learnedWords' alanını belirtilen miktar kadar artır/azalt.
            userDocRef.update("learnedWords", FieldValue.increment(amount.toLong())).await()
        } catch (e: Exception) {
            println("Toplam öğrenilen kelime güncelleme hatası: ${e.message}")
        }
    }
}