package com.esma.bunble.data.repository

import com.esma.bunble.domain.model.LearnItem
import com.esma.bunble.domain.model.LearningCategory
import com.esma.bunble.domain.repository.ILearningRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LearningRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ILearningRepository {
    override suspend fun getCategories(languagePath: String): List<LearningCategory> {
        return try {
            val snapshot = firestore.collection("languages").document(languagePath)
                .collection("categories").get().await()
            snapshot.documents.mapNotNull { doc ->
                LearningCategory(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()

        }
    }

    override suspend fun getPhrases(
        languagePath: String,
        categoryId: String
    ): List<LearnItem> {
        return try {
            val snapshot = firestore.collection("languages").document(languagePath)
                .collection("categories").document(categoryId)
                .collection("phrases").get().await()

            snapshot.documents.mapNotNull { doc ->
                LearnItem(
                    text = doc.getString("text") ?: "",
                    phonetic = doc.getString("phonetic") ?: "",
                    meaning = doc.getString("meaning") ?: "",
                    translation = doc.getString("translation") ?: "",

                    imageUrl = doc.getString("imageUrl") ?: ""
                )

            }

        }catch (e : Exception){
            emptyList()
        }
    }

}