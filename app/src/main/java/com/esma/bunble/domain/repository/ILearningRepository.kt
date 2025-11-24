package com.esma.bunble.domain.repository

import com.esma.bunble.domain.model.LearnItem
import com.esma.bunble.domain.model.LearningCategory

interface ILearningRepository {
    // Kullanıcının seçtiği dile göre kategorileri getir
    suspend fun getCategories(languagePath: String): List<LearningCategory>

    // Belirli bir kategorideki ifadeleri getir
    suspend fun getPhrases(languagePath: String, categoryId: String): List<LearnItem>

    // TODO: Kelimeler ve Quiz için de benzer fonksiyonlar eklenecek
}