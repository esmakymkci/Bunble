package com.esma.bunble.data.repository

import com.esma.bunble.domain.model.LearnItem
import com.esma.bunble.domain.model.LearningCategory
import com.esma.bunble.domain.model.QuizItem
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

                    imageUrl = doc.getString("imageUrl") ?: "",
                    audioUrl = doc.getString("audioUrl") ?:""
                )

            }

        }catch (e : Exception){
            emptyList()
        }
    }

    override suspend fun getWords(
        languagePath: String,        categoryId: String
    ): List<LearnItem> {
        return try {
            val snapshot = firestore.collection("languages").document(languagePath)
                .collection("categories").document(categoryId)
                .collection("words")
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                LearnItem(
                    text = doc.getString("text") ?: "",
                    phonetic = doc.getString("phonetic") ?: "",
                    meaning = doc.getString("meaning") ?: "",
                    translation = doc.getString("translation") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: "",
                    audioUrl = doc.getString("audioUrl") ?:""
                )
            }
        } catch (e : Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getCategoryDetails(
        languagePath: String,
        categoryId: String
    ): LearningCategory? {
        return try {
            val document = firestore.collection("languages")
                .document(languagePath)
                .collection("categories")
                .document(categoryId)
                .get()
                .await()

            if (document.exists()) {
                LearningCategory(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    imageUrl = document.getString("imageUrl") ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getQuizItems(
        languagePath: String,
        categoryId: String
    ): List<QuizItem> {
        return try {
            val snapshot = firestore.collection("languages").document(languagePath)
                .collection("categories").document(categoryId)
                .collection("quizes")
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                when (doc.getString("type")) {
                    "IMAGE_CHOICE" -> doc.toObject(QuizItem.ImageChoice::class.java)?.copy(id = doc.id)
                    "MULTIPLE_CHOICE" -> doc.toObject(QuizItem.MultipleChoice::class.java)?.copy(id = doc.id)
                    "TRUE_FALSE" -> doc.toObject(QuizItem.TrueFalse::class.java)?.copy(id = doc.id)
                    else -> QuizItem.Unsupported(id = doc.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


}