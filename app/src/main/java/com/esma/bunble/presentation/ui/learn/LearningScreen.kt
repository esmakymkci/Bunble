package com.esma.bunble.presentation.ui.learn

import com.esma.bunble.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.esma.bunble.domain.model.QuizItem
import com.esma.bunble.presentation.theme.ui.BorderGray
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandWhite
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.BrandYellowDark
import com.esma.bunble.presentation.theme.ui.BrandYellowLight
import com.esma.bunble.presentation.theme.ui.CorrectGreen
import com.esma.bunble.presentation.theme.ui.DarkPurple
import com.esma.bunble.presentation.theme.ui.GrayText
import com.esma.bunble.presentation.theme.ui.IncorrectRed
import com.esma.bunble.presentation.theme.ui.LightCorrectGreen
import com.esma.bunble.presentation.theme.ui.LightIncorrectRed
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.learn.AnswerState
import com.esma.bunble.presentation.viewmodel.learn.LearnState
import com.esma.bunble.presentation.viewmodel.learn.LearnViewModel
import com.esma.bunble.presentation.viewmodel.learn.QuizState

@Composable
fun LearningScreen(
    navController: NavController,
    viewModel: LearnViewModel = hiltViewModel()
) {
    val learnState = viewModel.learnState.value
    val quizState = viewModel.quizState.value
    val contentType = viewModel.contentType

    // İçerik tipine göre hangi arayüzün gösterileceğine karar verme
    when (contentType) {
        "quiz" -> {
            QuizContent(
                navController = navController,
                state = quizState,
                viewModel = viewModel
            )
        }
        else -> {
            LearnContent(
                navController = navController,
                state = learnState,
                viewModel = viewModel
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnContent(
    navController: NavController,
    state: LearnState,
    viewModel: LearnViewModel
) {
    val currentItem = state.currentItem

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.learn_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.learn_back_button_desc))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = SurfaceLight
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.error}", textAlign = TextAlign.Center)
                }
            }
            state.items.isEmpty() && !state.isLoading -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(id = R.string.learn_no_content), textAlign = TextAlign.Center)
                }
            }
            currentItem != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(currentItem.imageUrl),
                            contentDescription = currentItem.text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(text = currentItem.text, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = currentItem.translation,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        if (!currentItem.meaning.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = currentItem.meaning,
                                fontSize = 14.sp,
                                color = GrayText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        TranslationCard(
                            phonetic = currentItem.phonetic,
                            audioUrl = currentItem.audioUrl,
                            onPlayAudio = { viewModel.playAudio(currentItem.audioUrl) }
                        )
                    }
                    Button(
                        onClick = {
                            if (!state.isFinished) {
                                viewModel.onContinueClicked()
                            } else {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandYellow)
                    ) {
                        Text(
                            text = if (!state.isFinished) stringResource(id = R.string.learn_button_continue) else stringResource(id = R.string.learn_button_finish),
                            color = BrandBlack,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}


@Composable
fun TranslationCard(
    phonetic: String,
    audioUrl: String?,
    onPlayAudio: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWhite)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onPlayAudio,
                    enabled = !audioUrl.isNullOrBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = stringResource(id = R.string.learn_pronounce_button_desc),
                        modifier = Modifier
                            .size(40.dp)
                            .background(BrandYellowLight.copy(alpha = 0.2f), CircleShape)
                            .padding(8.dp),
                        tint = if (!audioUrl.isNullOrBlank()) BrandYellowDark else GrayText
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = phonetic, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = stringResource(id = R.string.learn_phonetic_label), color = GrayText, fontSize = 12.sp)
                }
            }
        }
    }
}

// QUIZ

@Composable
fun QuizContent(
    navController: NavController,
    state: QuizState,
    viewModel: LearnViewModel
) {
    Scaffold(containerColor = SurfaceLight) { innerPadding ->
        if (state.isQuizFinished) {
            QuizResultScreen(
                score = state.score,
                totalQuestions = state.questions.size,
                onFinish = { navController.popBackStack() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                QuizTopAppBar(
                    current = state.currentQuestionIndex + 1,
                    total = state.questions.size,
                    onClose = { navController.popBackStack() }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator()
                    } else if (state.questions.isNotEmpty()) {
                        val currentQuestion = state.currentQuestion
                        if (currentQuestion != null) {
                            when (currentQuestion) {
                                is QuizItem.ImageChoice -> ImageChoiceQuestionUI(
                                    question = currentQuestion,
                                    state = state,
                                    onAnswerSelected = { viewModel.onAnswerSelected(it) }
                                )
                                is QuizItem.MultipleChoice -> MultipleChoiceQuestionUI(
                                    question = currentQuestion,
                                    state = state,
                                    onAnswerSelected = { viewModel.onAnswerSelected(it) }
                                )
                                is QuizItem.TrueFalse -> TrueFalseQuestionUI(
                                    question = currentQuestion,
                                    state = state,
                                    onAnswerSelected = { viewModel.onAnswerSelected(it) }
                                )
                                is QuizItem.Unsupported -> Text(stringResource(id = R.string.quiz_unsupported_question))
                            }
                        }
                    } else if (state.error == null) {
                        Text(text = stringResource(id = R.string.quiz_no_questions))                    } else {
                        Text(text = stringResource(id = state.error))
                    }
                }
                QuizBottomBar(
                    answerState = state.answerState,
                    selectedAnswer = state.selectedAnswer,
                    onCheckOrNext = { viewModel.onCheckOrNextClicked() },
                    isLastQuestion = state.currentQuestionIndex == state.questions.size - 1 && state.questions.isNotEmpty()
                )
            }
        }
    }
}

@Composable
fun QuizTopAppBar(current: Int, total: Int, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.quiz_close_button_desc), modifier = Modifier.clickable(onClick = onClose))
        Spacer(Modifier.width(16.dp))
        val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(CircleShape),
            color = BrandYellow
        )
    }
}


@Composable
fun QuizBottomBar(
    answerState: AnswerState,
    selectedAnswer: Any?,
    onCheckOrNext: () -> Unit,
    isLastQuestion: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        /*
        if (answerState != AnswerState.UNANSWERED) {
            val (backgroundColor, text, icon) = if (answerState == AnswerState.CORRECT) {
                Triple(lightGreen, "Correct!", Icons.Default.Check)
            } else {
                Triple(lightRed, "Incorrect!", Icons.Default.Close)
            }
            Row(...) { ... }
        }
        */
        Button(
            onClick = onCheckOrNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp), // Alt boşluğu koru
            enabled = selectedAnswer != null,
            colors = ButtonDefaults.buttonColors(containerColor = BrandYellow),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = when {
                    answerState == AnswerState.UNANSWERED -> stringResource(id = R.string.quiz_button_check)
                    isLastQuestion -> stringResource(id = R.string.learn_button_finish) // "Finish"i tekrar kullanabiliriz
                    else -> stringResource(id = R.string.quiz_button_next)
                },
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )
        }
    }
}

@Composable
fun ImageChoiceQuestionUI(question: QuizItem.ImageChoice, state: QuizState, onAnswerSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(stringResource(id = R.string.quiz_image_choice_question), fontSize = 20.sp, color = GrayText)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(id = R.string.quiz_image_choice_question_format, question.questionText), fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(question.options) { imageUrl ->
                val isSelected = state.selectedAnswer == imageUrl
                var borderColor = if (isSelected) BrandYellow else Color.Transparent
                if (state.answerState != AnswerState.UNANSWERED) {
                    borderColor = when {
                        imageUrl == question.correctAnswer -> CorrectGreen
                        isSelected && imageUrl != question.correctAnswer -> IncorrectRed
                        else -> Color.Transparent
                    }
                }
                Box(modifier = Modifier
                    .aspectRatio(1f)
                    .border(4.dp, borderColor, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAnswerSelected(imageUrl) }) {
                    Image(painter = rememberAsyncImagePainter(imageUrl), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun MultipleChoiceQuestionUI(question: QuizItem.MultipleChoice, state: QuizState, onAnswerSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(stringResource(id = R.string.quiz_multiple_choice_question, question.questionText), fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            question.options.forEach { option ->
                OptionRow(text = option, isSelected = state.selectedAnswer == option, answerState = state.answerState, isCorrect = option == question.correctAnswer, onClick = { onAnswerSelected(option) })
            }
        }
    }
}

@Composable
fun TrueFalseQuestionUI(question: QuizItem.TrueFalse, state: QuizState, onAnswerSelected: (Boolean) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(stringResource(id = R.string.quiz_true_false_title), fontSize = 20.sp, color = GrayText)
        Spacer(Modifier.height(16.dp))
        Text(question.questionText, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OptionRow(text = stringResource(id = R.string.quiz_option_true), isSelected = state.selectedAnswer == true, answerState = state.answerState, isCorrect = question.correctAnswer, onClick = { onAnswerSelected(true) })
            OptionRow(text = stringResource(id = R.string.quiz_option_false), isSelected = state.selectedAnswer == false, answerState = state.answerState, isCorrect = !question.correctAnswer, onClick = { onAnswerSelected(false) })
        }
    }
}

@Composable
fun OptionRow(text: String, isSelected: Boolean, answerState: AnswerState, isCorrect: Boolean, onClick: () -> Unit) {
    val borderColor = when { !isSelected -> BorderGray; answerState == AnswerState.UNANSWERED -> BrandYellow; isCorrect -> CorrectGreen; else -> IncorrectRed }
    val backgroundColor = when { answerState == AnswerState.UNANSWERED -> Color.Transparent; isCorrect -> LightCorrectGreen; isSelected && !isCorrect -> LightIncorrectRed; else -> Color.Transparent }
    val icon = when { answerState == AnswerState.UNANSWERED -> null; isCorrect -> Icons.Default.Check; isSelected && !isCorrect -> Icons.Default.Close; else -> null }
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .border(2.dp, borderColor, RoundedCornerShape(16.dp))
        .background(backgroundColor, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        if (icon != null) { Icon(icon, contentDescription = null, tint = borderColor) }
    }
}

@Composable
fun QuizResultScreen(
    score: Int,
    totalQuestions: Int,
    onFinish: () -> Unit
) {
    val successRate = if (totalQuestions > 0) {
        (score.toFloat() / totalQuestions.toFloat() * 100).toInt()
    } else {
        0
    }
    val progress = if (totalQuestions > 0) score.toFloat() / totalQuestions.toFloat() else 0f

    val bunnyImageRes = if (successRate >= 80) {
        R.drawable.happy_bunny
    } else {
        R.drawable.sad_bunny
    }

    val titleColor = DarkPurple
    val successColor = CorrectGreen

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = bunnyImageRes),
            contentDescription = stringResource(id = R.string.quiz_result_background_desc),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(id = R.string.quiz_result_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.quiz_result_success_rate, successRate),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = successColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.quiz_result_questions_correct, score, totalQuestions),
                fontSize = 16.sp,
                color = GrayText
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = successColor,
                    trackColor = BrandWhite.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onFinish,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandYellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.quiz_result_back_to_category),
                        color = BrandBlack,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

