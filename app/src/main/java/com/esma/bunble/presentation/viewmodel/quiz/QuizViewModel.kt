package com.esma.bunble.presentation.viewmodel.quiz

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.esma.bunble.domain.model.QuizItem
import com.esma.bunble.domain.repository.ILearningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class QuizScreenState(
    val isLoading: Boolean = true,
    val questions: List<QuizItem> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val selectedAnswer: Any? = null,
    val answerState: AnswerState = AnswerState.UNANSWERED,
    val isQuizFinished: Boolean = false
)

// Cevap durumlarını yönetmek için bir enum class
enum class AnswerState { UNANSWERED, CORRECT, INCORRECT }

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: ILearningRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(QuizScreenState())
    val state: State<QuizScreenState> = _state

    init {
        val categoryId: String? = savedStateHandle.get("categoryId")
        if (categoryId != null) {
            loadQuiz(categoryId)
        } else {
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private fun loadQuiz(categoryId: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _state.value = _state.value.copy(isLoading = false); return@launch
            }
            try {
                // Kullanıcının dil yolunu Firestore'dan al
                val userDoc = firestore.collection("users").document(user.uid).get().await()
                val languagePath = userDoc.getString("languagePath") ?: "tr-de"

                val quizItems = repository.getQuizItems(languagePath, categoryId).shuffled()
                _state.value = _state.value.copy(
                    questions = quizItems,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                e.printStackTrace()
            }
        }
    }


    //  Kullanıcı bir seçenek seçtiğinde çağrılır
    fun onAnswerSelected(answer: Any) {
        // Eğer soru zaten cevaplandıysa, tekrar seçim yapmayı engelle
        if (_state.value.answerState != AnswerState.UNANSWERED) return

        _state.value = _state.value.copy(selectedAnswer = answer)
    }

    fun onCheckOrNextClicked() {
        if (_state.value.answerState == AnswerState.UNANSWERED) {
            checkAnswer()
        } else {
            goToNextQuestion()
        }
    }

    private fun checkAnswer() {
        val currentQuestion = _state.value.questions.getOrNull(_state.value.currentQuestionIndex) ?: return
        val correctAnswer = when (currentQuestion) {
            is QuizItem.ImageChoice -> currentQuestion.correctAnswer
            is QuizItem.MultipleChoice -> currentQuestion.correctAnswer
            is QuizItem.TrueFalse -> currentQuestion.correctAnswer
            is QuizItem.Unsupported -> null
        }

        if (_state.value.selectedAnswer == correctAnswer) {
            _state.value = _state.value.copy(
                answerState = AnswerState.CORRECT,
                //  doğru cevap sayısı
                score = _state.value.score + 1
            )
        } else {
            _state.value = _state.value.copy(answerState = AnswerState.INCORRECT)
        }
    }

    private fun goToNextQuestion() {
        val nextIndex = _state.value.currentQuestionIndex + 1
        if (nextIndex < _state.value.questions.size) {
            _state.value = _state.value.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswer = null,
                answerState = AnswerState.UNANSWERED
            )
        } else {
            _state.value = _state.value.copy(isQuizFinished = true)
        }
    }
}

