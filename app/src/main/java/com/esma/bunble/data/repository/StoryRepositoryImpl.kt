package com.esma.bunble.data.repository

import com.esma.bunble.domain.model.Story
import com.esma.bunble.domain.repository.IStoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : IStoryRepository {

    override suspend fun getStories(targetLanguage: String): List<Story> {
        // Bu fonksiyonun implementasyonu şimdilik boş kalsın, ViewModel'da yapacağız.
        //  ViewModel'da sourceLanguage'a da ihtiyacımız var.
        return emptyList()
    }

    override suspend fun getStoryDetail(storyId: String): Map<String, Any> {
        val storyDoc = firestore.collection("stories").document(storyId).get().await()
        val contentDoc = firestore.collection("story_content").document(storyId).get().await()

        return mapOf(
            "story" to storyDoc,
            "content" to contentDoc
        )
    }
}
