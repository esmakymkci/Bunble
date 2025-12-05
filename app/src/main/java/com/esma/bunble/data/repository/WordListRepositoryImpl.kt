package com.esma.bunble.data.repository

import com.esma.bunble.domain.model.Word
import com.esma.bunble.domain.model.WordList
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
    private val auth: FirebaseAuth
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
            // Olası bir hata durumunda burada loglama yapabilirsin.
            // Log.e("Firestore", "Add word transaction failed: ${e.message}")
        }
    }

    override suspend fun updateWord(listId: String, card: Word) {
        val collection = listsCollection ?: return
        val listDocumentRef = collection.document(listId)
        val wordDocumentRef = listDocumentRef.collection("words").document(card.id)

        try {
            firestore.runTransaction { transaction ->
                // Önce güncellenecek kelimenin mevcut durumunu transaction içinde oku.
                // Bu, artırma mı azaltma mı yapacağımızı belirlemek için gereklidir.
                val snapshot = transaction.get(wordDocumentRef)
                val existingWord = snapshot.toObject(Word::class.java)

                // Eğer kelime bulunamazsa veya öğrenilme durumu değişmemişse işlem yapma.
                if (existingWord == null || existingWord.isLearned == card.isLearned) {
                    // Sadece kelimenin diğer bilgilerini güncelle (belki metni değişti vs.)
                    transaction.set(wordDocumentRef, card)
                    return@runTransaction null // Transaction'ı bitir
                }

                // 'learnedCount' için artış miktarını belirle.
                // Yeni durum 'true' ise +1, 'false' ise -1 artır.
                val increment = if (card.isLearned) 1L else -1L

                // Ana liste dokümanındaki 'learnedCount' alanını atomik olarak güncelle.
                transaction.update(listDocumentRef, "learnedCount",
                    FieldValue.increment(increment))

                // Kelime dokümanının kendisini yeni haliyle güncelle.
                transaction.set(wordDocumentRef, card)

                null // Transaction başarılı
            }.await()
        } catch (e: Exception) {

        }
    }

    override suspend fun deleteWord(listId: String, wordId: String, isLearned: Boolean) {
        val listDocRef = listsCollection?.document(listId) ?: return
        val wordDocRef = listDocRef.collection("words").document(wordId)

        firestore.runTransaction { transaction ->
            // Kelimeyi direkt sil.
            transaction.delete(wordDocRef)

            // Toplam kelime sayacını her zaman azalt.
            transaction.update(listDocRef, "wordCount", FieldValue.increment(-1))

            // EĞER kelime öğrenilmişse (bu bilgi artık UI'dan geliyor),
            // öğrenilmiş kelime sayacını azalt.
            if (isLearned) {
                transaction.update(listDocRef, "learnedCount", FieldValue.increment(-1))
            }
        }.await()
    }


    override suspend fun deleteList(listId: String) {
        val listDocument = listsCollection?.document(listId) ?: return
        try {
            listDocument.delete().await()

        } catch (e: Exception) {
            // Hata yönetimi
        }
    }
}
