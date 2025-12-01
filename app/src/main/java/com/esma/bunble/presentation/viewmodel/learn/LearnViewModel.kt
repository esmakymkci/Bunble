package com.esma.bunble.presentation.viewmodel.learn

import android.app.Application
import android.media.MediaPlayer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.esma.bunble.domain.model.QuizItem
import com.esma.bunble.domain.repository.ILearningRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.esma.bunble.R

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val repository: ILearningRepository,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val application: Application,
    private val imageLoader: ImageLoader,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _learnState = mutableStateOf(LearnState())
    val learnState: State<LearnState> = _learnState

    private val _quizState = mutableStateOf(QuizState())
    val quizState: State<QuizState> = _quizState

    val contentType: String?

    private var mediaPlayer: MediaPlayer? = null

    init {
        val categoryId: String? = savedStateHandle.get("categoryId")
        contentType = savedStateHandle.get("type")

        if (categoryId != null) {
            when (contentType) {
                "words" -> loadLearnItems(categoryId, "words")
                "phrases" -> loadLearnItems(categoryId, "phrases")
                "quiz" -> loadQuizItems(categoryId)
                else -> loadLearnItems(categoryId, "words") // Varsayılan
            }
        } else {
            val errorId = R.string.error_category_id_not_found
            _learnState.value = LearnState(isLoading = false, error = errorId)
            _quizState.value = QuizState(isLoading = false, error = errorId)
        }
    }

    private fun loadLearnItems(categoryId: String, type: String) {
        viewModelScope.launch {
            _learnState.value = _learnState.value.copy(isLoading = true)
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                _learnState.value = LearnState(isLoading = false, error = R.string.error_user_not_logged_in)
                return@launch
            }
            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val languagePath = userDoc.getString("languagePath")
                if (languagePath != null) {
                    val itemsResult = if (type == "words") repository.getWords(languagePath, categoryId) else repository.getPhrases(languagePath, categoryId)

                    itemsResult.forEach { learnItem ->
                        val request = ImageRequest.Builder(application)
                            .data(learnItem.imageUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                        imageLoader.enqueue(request)
                    }

                    _learnState.value = LearnState(items = itemsResult.shuffled(), isLoading = false)
                } else {
                    _learnState.value = LearnState(isLoading = false, error =  R.string.error_language_path_not_found)
                }
            } catch (e: Exception) {
                _learnState.value = LearnState(isLoading = false, error = R.string.error_unknown)
            }
        }
    }

    fun onContinueClicked() {
        val currentState = _learnState.value
        if (!currentState.isFinished) {
            _learnState.value = currentState.copy(currentItemIndex = currentState.currentItemIndex + 1)
        }
    }

    private fun loadQuizItems(categoryId: String) {
        viewModelScope.launch {
            _quizState.value = _quizState.value.copy(isLoading = true)
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                _quizState.value = QuizState(isLoading = false, error = R.string.error_user_not_logged_in)
                return@launch
            }
            try {
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                val languagePath = userDoc.getString("languagePath")
                if (languagePath != null) {
                    val quizItemsResult = repository.getQuizItems(languagePath, categoryId)

                    quizItemsResult.forEach { quizItem ->
                        if (quizItem is QuizItem.ImageChoice) {
                            quizItem.options.forEach { imageUrl ->
                                val request = ImageRequest.Builder(application)
                                    .data(imageUrl)
                                    .build()
                                imageLoader.enqueue(request)
                            }
                        }
                    }
                    _quizState.value = QuizState(questions = quizItemsResult.shuffled(), isLoading = false)
                } else {
                    _quizState.value = QuizState(isLoading = false, error = R.string.error_language_path_not_found)
                }
            } catch (e: Exception) {
                _quizState.value = QuizState(isLoading = false, error = R.string.error_unknown)
            }
        }
    }

    fun onAnswerSelected(answer: Any) {
        if (_quizState.value.answerState != AnswerState.UNANSWERED) return
        _quizState.value = _quizState.value.copy(selectedAnswer = answer)
    }

    fun onCheckOrNextClicked() {
        if (_quizState.value.answerState == AnswerState.UNANSWERED) {
            checkAnswer()
        } else {
            goToNextQuestion()
        }
    }

    private fun checkAnswer() {
        val currentQuestion = _quizState.value.currentQuestion ?: return
        val correctAnswer = when (currentQuestion) {
            is QuizItem.ImageChoice -> currentQuestion.correctAnswer
            is QuizItem.MultipleChoice -> currentQuestion.correctAnswer
            is QuizItem.TrueFalse -> currentQuestion.correctAnswer
            is QuizItem.Unsupported -> null
        }

        if (_quizState.value.selectedAnswer == correctAnswer) {
            _quizState.value = _quizState.value.copy(
                answerState = AnswerState.CORRECT,
                score = _quizState.value.score + 1
            )
        } else {
            _quizState.value = _quizState.value.copy(answerState = AnswerState.INCORRECT)
        }
    }

    private fun goToNextQuestion() {
        val nextIndex = _quizState.value.currentQuestionIndex + 1
        if (nextIndex < _quizState.value.questions.size) {
            _quizState.value = _quizState.value.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswer = null,
                answerState = AnswerState.UNANSWERED
            )
        } else {
            _quizState.value = _quizState.value.copy(isQuizFinished = true)
        }
    }

    fun playAudio(audioUrl: String?) {
        if (audioUrl.isNullOrBlank()) return
        mediaPlayer?.release()
        mediaPlayer = null
        viewModelScope.launch {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioUrl)
                    prepareAsync()
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener { it.release(); mediaPlayer = null }
                    setOnErrorListener { mp, _, _ -> mp.release(); mediaPlayer = null; true }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}
