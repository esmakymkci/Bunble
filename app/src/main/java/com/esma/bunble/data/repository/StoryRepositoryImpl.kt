package com.esma.bunble.data.repository

import android.util.Log
import com.esma.bunble.data.local.UserPreferencesRepository
import com.esma.bunble.domain.model.PublicStory
import com.esma.bunble.domain.model.StoryDetail
import com.esma.bunble.domain.model.UserStory
import com.esma.bunble.domain.repository.IStoryRepository
import com.esma.bunble.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.nl.languageid.LanguageIdentification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userPrefs: UserPreferencesRepository
) : IStoryRepository {

    override fun getPublicStories(targetLanguage: String): Flow<Resource<List<PublicStory>>> = flow     {
        emit(Resource.Loading())
        try {
            val snapshot = firestore.collection("stories")
                .whereArrayContains("available_languages", targetLanguage)
                .get()
                .await()

            val stories = snapshot.documents.mapNotNull { doc ->
                PublicStory(
                    id = doc.id,
                    title = doc.getString("title_$targetLanguage") ?: "No title",
                    difficulty = doc.getString("difficulty") ?: "Beginner",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
            emit(Resource.Success(stories))
        } catch (e: Exception) {
            Log.e("StoryRepository", "Error getting public stories", e)
            emit(Resource.Error(e.localizedMessage ?: "Failed to load public stories"))
        }
    }

    override fun getUserStories(): Flow<Resource<List<UserStory>>> = flow {
        emit(Resource.Loading())
        val userId = auth.currentUser?.uid
        if (userId == null) {
            emit(Resource.Success(emptyList()))
            return@flow
        }

        try {
            val snapshot = firestore.collection("users").document(userId)
                .collection("user_stories")
                .get()
                .await()

            // Dökümanları UserStory modeline dönüştür
            val stories = snapshot.documents.mapNotNull { doc ->
                val content = doc.getString("content") ?: ""
                val contentSnippet = if (content.length > 100) content.substring(0, 100) + "..." else content

                UserStory(
                    id = doc.id,
                    title = doc.getString("title") ?: "No title",
                    content = contentSnippet
                )
            }
            emit(Resource.Success(stories))
        } catch (e: Exception) {
            Log.e("StoryRepository", "Error getting user stories", e)
            emit(Resource.Error(e.localizedMessage ?: "Failed to load your stories"))
        }
    }

    override suspend fun getStoryDetail(storyId: String): Resource<StoryDetail> {
        return try {
            val userId = auth.currentUser?.uid
            val targetLang = userPrefs.targetLanguage.first()
                ?: return Resource.Error("Target language preference not found.")

            //  ÖNCE HALKA AÇIK 'stories' KOLEKSİYONUNDA ARA
            val publicStoryDoc = firestore.collection("stories").document(storyId).get().await()
            if (publicStoryDoc.exists()) {
                val contentDoc = firestore.collection("story_content").document(storyId).get().await()
                if (!contentDoc.exists()) {
                    return Resource.Error("Story content not found for public story.")
                }

                @Suppress("UNCHECKED_CAST")
                val contentMap = contentDoc.get("content") as? Map<String, Any>

                val storyDetail = StoryDetail(
                    id = publicStoryDoc.id,
                    title = publicStoryDoc.getString("title_$targetLang") ?: "No Title",
                    content = contentMap?.get(targetLang)?.toString() ?: "Content not available.",
                    imageUrl = publicStoryDoc.getString("imageUrl"),
                    isUserStory = false,
                    authorId = publicStoryDoc.getString("authorId")
                )
                return Resource.Success(storyDetail)
            }
            // HALKA AÇIK HİKAYE BULUNAMADIYSA, KULLANICI HİKAYELERİNDE ARA
            if (userId != null) {
                val userStoryDoc = firestore.collection("users").document(userId)
                    .collection("user_stories").document(storyId).get().await()

                if (userStoryDoc.exists()) {
                    val storyDetail = StoryDetail(
                        id = userStoryDoc.id,
                        title = userStoryDoc.getString("title") ?: "No Title",
                        content = userStoryDoc.getString("content") ?: "Content not available.",
                        imageUrl = null,
                        isUserStory = true,
                        authorId = userId // Yazar, mevcut kullanıcıdır
                    )
                    return Resource.Success(storyDetail)
                }
            }

            return Resource.Error("Story not found.")


        } catch (e: Exception) {
            Log.e("StoryRepository", "Error getting story detail for $storyId", e)
            Resource.Error(e.message ?: "An unexpected error occurred while fetching the story.")
        }
    }

    override suspend fun deleteUserStory(storyId: String): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                return Resource.Error("User not logged in.")
            }

            // Firestore'dan ilgili hikayeyi sil
            firestore.collection("users").document(userId)
                .collection("user_stories").document(storyId)
                .delete()
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An unexpected error occurred.")
        }
    }

    override suspend fun addUserStory(title: String, content: String): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in.")

            // Kullanıcı hangi dilde yazdıysa o dilde kaydedilir.
            val storyData = mapOf(
                "title" to title,
                "content" to content,
                "createdAt" to FieldValue.serverTimestamp()
            )

            // Veriyi 'users/{userId}/user_stories' altına yaz.
            firestore.collection("users").document(userId)
                .collection("user_stories")
                .add(storyData)
                .await()

            Resource.Success(Unit)

        } catch (e: Exception) {
            Log.e("addUserStory", "Error adding user story", e)
            Resource.Error(e.localizedMessage ?: "An unknown error occurred.")
        }
    }

    override suspend fun detectLanguage(text: String): String? {
        return try {
            val languageIdentifier = LanguageIdentification.getClient()
            val languageCode = languageIdentifier.identifyLanguage(text).await()
            if (languageCode == "und") { // "und" -> undetermined
                null
            } else {
                languageCode
            }
        } catch (e: Exception) {
            Log.e("detectLanguage", "Error identifying language", e)
            null
        }
    }

    override suspend fun getUserStory(storyId: String): Resource<UserStory> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Resource.Error("User not logged in.")

            val document = firestore.collection("users").document(userId)
                .collection("user_stories").document(storyId).get().await()

            val story = document.toObject(UserStory::class.java)

            if (story != null) {
                // Burada tüm içeriği döndürmeliyiz, kesilmiş halini değil.
                Resource.Success(story)
            } else {
                Resource.Error("Story not found.")
            }
        } catch (e: Exception) {
            Log.e("StoryRepository", "Error getting user story: $storyId", e)
            Resource.Error(e.message ?: "An unknown error occurred.")
        }
    }

    override suspend fun updateUserStory(storyId: String, title: String, content: String): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Resource.Error("User not logged in.")

            val storyRef = firestore.collection("users").document(userId)
                .collection("user_stories").document(storyId)

            // 'update' metodu ile sadece belirtilen alanları güncelle
            storyRef.update(
                mapOf(
                    "title" to title,
                    "content" to content
                )
            ).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("StoryRepository", "Error updating user story: $storyId", e)
            Resource.Error(e.message ?: "An unknown error occurred.")
        }
    }
}
