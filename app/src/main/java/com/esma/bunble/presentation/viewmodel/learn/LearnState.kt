package com.esma.bunble.presentation.viewmodel.learn

import androidx.annotation.StringRes
import com.esma.bunble.domain.model.LearnItem
import com.esma.bunble.domain.model.QuizItem

data class LearnState(
    val items: List<LearnItem> = emptyList(),
    val currentItemIndex: Int = 0,
    val isLoading: Boolean = true,
    @StringRes val error: Int? = null
) {
    val currentItem: LearnItem? get() = items.getOrNull(currentItemIndex)
    val isFinished: Boolean get() = currentItemIndex >= items.size - 1 && items.isNotEmpty()
}

enum class AnswerState { UNANSWERED, CORRECT, INCORRECT }

data class QuizState(
    val isLoading: Boolean = true,
    val questions: List<QuizItem> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val selectedAnswer: Any? = null,
    val answerState: AnswerState = AnswerState.UNANSWERED,
    val isQuizFinished: Boolean = false,
    @StringRes val error: Int? = null
) {
    val currentQuestion: QuizItem? get() = questions.getOrNull(currentQuestionIndex)
}