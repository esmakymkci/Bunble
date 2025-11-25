package com.esma.bunble.domain.repository

import com.esma.bunble.domain.model.LearnItem
import com.esma.bunble.domain.model.LearningCategory
import com.esma.bunble.domain.model.QuizItem

interface ILearningRepository {
    suspend fun getCategories(languagePath: String): List<LearningCategory>
    suspend fun getPhrases(languagePath: String, categoryId: String): List<LearnItem>
    suspend fun getWords(languagePath: String, categoryId: String): List<LearnItem>
    suspend fun getCategoryDetails(languagePath: String, categoryId: String): LearningCategory?
    suspend fun getQuizItems(languagePath: String, categoryId: String): List<QuizItem>
}