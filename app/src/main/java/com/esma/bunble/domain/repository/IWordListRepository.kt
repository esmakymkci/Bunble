package com.esma.bunble.domain.repository

import com.esma.bunble.domain.model.Word
import com.esma.bunble.domain.model.WordList
import kotlinx.coroutines.flow.Flow

interface IWordListRepository {

    // Kullanıcının tüm kelime listelerini getirir.
    fun getAllLists(): Flow<List<WordList>>

    // Belirli bir listenin detaylarını (başlık, dil vs.) getirir.
    fun getListDetails(listId: String): Flow<WordList?>

    // Belirli bir listenin içindeki tüm kelime kartlarını getirir.
    fun getWordsForList(listId: String): Flow<List<Word>>

    // Yeni bir kelime listesi oluşturur.
    suspend fun createList(title: String, sourceLang: String, targetLang: String)

    // Bir listeye yeni bir kelime kartı ekler.
    suspend fun addWordToList(listId: String, card: Word)

    // Bir kelime kartını günceller (örn: isLearned durumunu değiştirmek için).
    suspend fun updateWord(listId: String, card: Word)

    // Bir kelime kartını siler.
    suspend fun deleteWord(listId: String, wordId: String,isLearned: Boolean)

    // Bir listeyi tamamen siler.
    suspend fun deleteList(listId: String)
}
