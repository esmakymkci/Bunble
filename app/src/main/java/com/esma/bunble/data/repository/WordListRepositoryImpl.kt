package com.esma.bunble.data.repository

import android.util.Log
import com.esma.bunble.domain.model.Word
import com.esma.bunble.domain.model.WordList
import com.esma.bunble.domain.repository.IUserRepository
import com.esma.bunble.domain.repository.IWordListRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WordListRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userRepository: IUserRepository
) : IWordListRepository {

    // Mevcut kullanıcının ID'sini almak için bir yardımcı özellik.
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Kullanıcının kelime listeleri koleksiyonuna bir referans.
    private val listsCollection
        get() = currentUserId?.let {
            firestore.collection("users").document(it).collection("word_lists")
        }



    override fun getAllLists(): Flow<List<WordList>> {
        val userId = auth.currentUser?.uid ?: return flowOf(emptyList())

        return firestore.collection("users").document(userId)
            .collection("word_lists")
            .snapshots() // Koleksiyonu dinle
            .map { snapshot ->
                snapshot.toObjects(WordList::class.java)            }
    }


    override fun getListDetails(listId: String): Flow<WordList?> = callbackFlow {
        val document = listsCollection?.document(listId)
        if (document == null) {
            trySend(null).isSuccess
            close()
            return@callbackFlow
        }

        // Firestore'daki doküman değişikliklerini CANLI olarak dinle
        val subscription = document.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Hata varsa akışı kapat
                return@addSnapshotListener
            }
            // Snapshot'ı WordList objesine çevir ve akışa gönder
            val wordList = snapshot?.toObject(WordList::class.java)
            trySend(wordList).isSuccess
        }

        // Bu kod bloğu, Flow dinlenmeyi bıraktığında çalışır.
        // Kaynak sızıntısını önlemek için dinleyiciyi kaldırır.
        awaitClose { subscription.remove() }
    }

    override fun getWordsForList(listId: String): Flow<List<Word>> = callbackFlow {
        val collection = listsCollection?.document(listId)?.collection("words")
        if (collection == null) {
            trySend(emptyList()).isSuccess
            close()
            return@callbackFlow
        }

        // 'words' alt koleksiyonundaki değişiklikleri CANLI olarak dinle
        val subscription = collection.orderBy("createdAt").addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Hata varsa akışı kapat
                return@addSnapshotListener
            }
            // Snapshot'ı Word listesine çevir ve akışa gönder
            val words = snapshot?.toObjects(Word::class.java) ?: emptyList()
            trySend(words).isSuccess
        }

        // Dinleyiciyi kaldır
        awaitClose { subscription.remove() }
    }

    override suspend fun createList(title: String, sourceLang: String, targetLang: String) {
        val collection = listsCollection ?: return
        try {
            // Otomatik ID ile yeni bir doküman oluştur
            val newListDocument = collection.document()
            val newList = WordList(
                id = newListDocument.id,
                title = title,
                sourceLang = sourceLang,
                targetLang = targetLang
            )
            newListDocument.set(newList).await()
        } catch (e: Exception) {
            // Hata yönetimi
        }
    }

    override suspend fun addWordToList(listId: String, card: Word) {
        // Gerekli referansları transaction dışında al
        val listDocumentRef = listsCollection?.document(listId) ?: return
        val newWordDocumentRef = listDocumentRef.collection("words").document()

        try {
            // Atomik bir işlem başlat
            firestore.runTransaction { transaction ->
                // Ana liste dokümanındaki 'wordCount' alanını sunucu tarafında 1 artır.
                transaction.update(listDocumentRef, "wordCount", FieldValue.increment(1))

                // 2. Yeni kelime dokümanını oluştur ve ID'sini ayarla.
                val newCard = card.copy(id = newWordDocumentRef.id)
                transaction.set(newWordDocumentRef, newCard)

                // Transaction'ın başarılı olduğunu belirtmek için null döndür.
                null
            }.await() // Transaction'ın bitmesini bekle

        } catch (e: Exception) {
            Log.e("Firestore", "Add word transaction failed: ${e.message}")
        }
    }

    override suspend fun updateWord(listId: String, card: Word) {
        val userId = currentUserId ?: return // Kullanıcı ID'sini al
        val listDocumentRef = listsCollection?.document(listId) ?: return
        val wordDocumentRef = listDocumentRef.collection("words").document(card.id)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(wordDocumentRef)
                val existingWord = snapshot.toObject(Word::class.java)

                if (existingWord == null || existingWord.isLearned == card.isLearned) {
                    transaction.set(wordDocumentRef, card)
                    return@runTransaction null
                }

                val increment = if (card.isLearned) 1L else -1L

                // Bu iki satır zaten vardı, liste içi sayaçları güncelliyor.
                transaction.update(listDocumentRef, "learnedCount", FieldValue.increment(increment))
                transaction.set(wordDocumentRef, card)

                // --- 3. YENİ KOD ---
                // Şimdi, genel kullanıcı istatistiklerini de güncelle.
                // userRepository.updateTotalLearnedWords(userId, increment.toInt())
                // NOT: Transaction içinde suspend fonksiyon çağıramayız. Bu yüzden bu işi dışarıda yapacağız.

                null
            }.await()
            // --- 4. YENİ KOD ---
            // Transaction başarılı olduktan sonra, genel sayacı güncelle.
            val increment = if (card.isLearned) 1 else -1
            updateTotalLearnedWords(userId, increment) // Ayrı bir suspend fonksiyona taşıdık.

        } catch (e: Exception) {
            // Hata yönetimi
        }
    }

    private suspend fun updateTotalLearnedWords(userId: String, amount: Int) {
        userRepository.updateTotalLearnedWords(userId, amount)
    }

    override suspend fun deleteWord(listId: String, wordId: String, isLearned: Boolean) {
        val userId = currentUserId ?: return // <-- Kullanıcı ID'sini fonksiyonun başında al
        val listDocRef = listsCollection?.document(listId) ?: return
        val wordDocRef = listDocRef.collection("words").document(wordId)

        firestore.runTransaction { transaction ->
            // ... transaction içeriği aynı kalacak ...
            transaction.delete(wordDocRef)
            transaction.update(listDocRef, "wordCount", FieldValue.increment(-1))
            if (isLearned) {
                transaction.update(listDocRef, "learnedCount", FieldValue.increment(-1))
            }
        }.await()

        // Eğer silinen kelime 'öğrenilmiş' olarak işaretlenmişse,
        // genel kullanıcı istatistiklerindeki toplam sayacı da 1 azalt.
        if (isLearned) {
            updateTotalLearnedWords(userId, -1)
        }
    }



    override suspend fun deleteList(listId: String) {
        val listDocRef = listsCollection?.document(listId) ?: return
        val wordsCollectionRef = listDocRef.collection("words")

        try {
            // Alt koleksiyondaki (words) tüm dokümanları al.
            val wordsSnapshot = wordsCollectionRef.get().await()

            // Bir toplu yazma işlemi (Batched Write) başlat.
            // Bu, tüm silme işlemlerini tek bir atomik istek olarak gönderir.
            val batch = firestore.batch()

            // Alınan her bir kelime dokümanı için batch'e bir silme komutu ekle.
            for (document in wordsSnapshot.documents) {
                batch.delete(document.reference)
            }

            // Ana liste dokümanını da silmek için batch'e ekle.
            batch.delete(listDocRef)

            //  Tüm silme komutlarını içeren batch'i sunucuya gönder ve çalıştır.
            batch.commit().await()

        } catch (e: Exception) {
            Log.e("Firestore", "Failed to delete list and its words: ${e.message}")
        }
    }
}
