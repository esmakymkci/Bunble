package com.esma.bunble.presentation.ui.learn

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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.esma.bunble.R
import com.esma.bunble.domain.model.QuizItem
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
                title = { Text(text = "Learning", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Help action */ }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.error}", textAlign = TextAlign.Center)
                }
            }
            state.items.isEmpty() && !state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Bu kategoride öğrenilecek içerik bulunamadı.", textAlign = TextAlign.Center)
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
                                color = Color.Gray,
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDD835))
                    ) {
                        Text(
                            text = if (!state.isFinished) "Continue" else "Finish",
                            color = Color.Black,
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        contentDescription = "Pronounce",
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFC107).copy(alpha = 0.2f), CircleShape)
                            .padding(8.dp),
                        tint = if (!audioUrl.isNullOrBlank()) Color(0xFFD4A000) else Color.Gray
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = phonetic, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Phonetic", color = Color.Gray, fontSize = 12.sp)
                }
            }
            IconButton(onClick = { /* TODO: Bookmark action */ }) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = Color.Gray)
            }
        }
    }
}

// QUIZ

val brandYellow = Color(0xFFFDD835)
val correctGreen = Color(0xFF4CAF50)
val incorrectRed = Color(0xFFF44336)
val lightGreen = correctGreen.copy(alpha = 0.1f)
val lightRed = incorrectRed.copy(alpha = 0.1f)

@Composable
fun QuizContent(
    navController: NavController,
    state: QuizState,
    viewModel: LearnViewModel
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
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
                                is QuizItem.Unsupported -> Text("Error: This question type is not supported.")
                            }
                        }
                    } else if (state.error == null) {
                        Text(text = "Bu kategori için henüz quiz sorusu bulunmuyor.")
                    } else {
                        Text(text = "Error: ${state.error}")
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
        Icon(Icons.Default.Close, contentDescription = "Close Quiz", modifier = Modifier.clickable(onClick = onClose))
        Spacer(Modifier.width(16.dp))
        val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(CircleShape),
            color = brandYellow
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
            colors = ButtonDefaults.buttonColors(containerColor = brandYellow),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = when {
                    answerState == AnswerState.UNANSWERED -> "Check"
                    isLastQuestion -> "Finish"
                    else -> "Next"
                },
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ImageChoiceQuestionUI(question: QuizItem.ImageChoice, state: QuizState, onAnswerSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Which image represents", fontSize = 20.sp, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        Text("'${question.questionText}'?", fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(question.options) { imageUrl ->
                val isSelected = state.selectedAnswer == imageUrl
                var borderColor = if (isSelected) brandYellow else Color.Transparent
                if (state.answerState != AnswerState.UNANSWERED) {
                    borderColor = when {
                        imageUrl == question.correctAnswer -> correctGreen
                        isSelected && imageUrl != question.correctAnswer -> incorrectRed
                        else -> Color.Transparent
                    }
                }
                Box(modifier = Modifier.aspectRatio(1f).border(4.dp, borderColor, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).clickable { onAnswerSelected(imageUrl) }) {
                    Image(painter = rememberAsyncImagePainter(imageUrl), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun MultipleChoiceQuestionUI(question: QuizItem.MultipleChoice, state: QuizState, onAnswerSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("What is the translation of\n'${question.questionText}'?", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
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
        Text("True or False?", fontSize = 20.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Text(question.questionText, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OptionRow(text = "True", isSelected = state.selectedAnswer == true, answerState = state.answerState, isCorrect = question.correctAnswer, onClick = { onAnswerSelected(true) })
            OptionRow(text = "False", isSelected = state.selectedAnswer == false, answerState = state.answerState, isCorrect = !question.correctAnswer, onClick = { onAnswerSelected(false) })
        }
    }
}

@Composable
fun OptionRow(text: String, isSelected: Boolean, answerState: AnswerState, isCorrect: Boolean, onClick: () -> Unit) {
    val borderColor = when { !isSelected -> Color.LightGray; answerState == AnswerState.UNANSWERED -> brandYellow; isCorrect -> correctGreen; else -> incorrectRed }
    val backgroundColor = when { answerState == AnswerState.UNANSWERED -> Color.Transparent; isCorrect -> lightGreen; isSelected && !isCorrect -> lightRed; else -> Color.Transparent }
    val icon = when { answerState == AnswerState.UNANSWERED -> null; isCorrect -> Icons.Default.Check; isSelected && !isCorrect -> Icons.Default.Close; else -> null }
    Row(modifier = Modifier.fillMaxWidth().height(56.dp).border(2.dp, borderColor, RoundedCornerShape(16.dp)).background(backgroundColor, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
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

    val titleColor = Color(0xFF4A148C)
    val successColor = Color(0xFF4CAF50)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = bunnyImageRes),
            contentDescription = "Quiz Result Background",
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
                text = "Quiz Completed!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$successRate% Success",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = successColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$score/$totalQuestions Questions Correct",
                fontSize = 16.sp,
                color = Color.Gray
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
                    trackColor = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onFinish,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE082)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Back to Category",
                        color = Color.Black,
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


