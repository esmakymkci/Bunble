package com.esma.bunble.presentation.ui.quiz

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.esma.bunble.presentation.viewmodel.quiz.AnswerState
import com.esma.bunble.presentation.viewmodel.quiz.QuizViewModel

// Ana Renkler (değişiklik yok)
val brandYellow = Color(0xFFFDD835)
val correctGreen = Color(0xFF4CAF50)
val incorrectRed = Color(0xFFF44336)
val lightGreen = correctGreen.copy(alpha = 0.1f)
val lightRed = incorrectRed.copy(alpha = 0.1f)

@Composable
fun QuizScreen(
    navController: NavController,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    // Scaffold'u, tüm ekranı kaplayacak şekilde ayarlayalım.
    Scaffold(        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding -> // Scaffold'un kendi padding'i (genellikle sıfır).

        // --- DEĞİŞİKLİK BURADA BAŞLIYOR ---
        // Quiz'in bitip bitmediğini KONTROL EDEREK doğru Composable'ı çizelim.
        if (state.isQuizFinished) {
            // 1. Quiz bittiyse, SADECE QuizResultScreen'i çiz.
            // Bu ekran kendi Insets'ini kendi yönetecek ve tam ekran olacak.
            QuizResultScreen(
                score = state.score,
                totalQuestions = state.questions.size,
                onFinish = { navController.popBackStack() }
            )
        } else {
            // 2. Quiz devam ediyorsa, Insets'e uyan bir Column çiz.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Bu padding'ler, içeriği sistem barlarının altına ve üstüne sıkışmaktan kurtarır.
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                // Quiz devam ediyorsa, tüm bileşenleri göster.
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
                        val currentQuestion = state.questions[state.currentQuestionIndex]
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
                    } else {
                        Text(text = "Bu kategori için henüz quiz sorusu bulunmuyor.")
                    }
                }

                QuizBottomBar(
                    answerState = state.answerState,
                    selectedAnswer = state.selectedAnswer,
                    onCheckOrNext = { viewModel.onCheckOrNextClicked() },
                    isLastQuestion = state.currentQuestionIndex == state.questions.size - 1
                )
            }
        }
    }
}




@Composable
fun QuizTopAppBar(current: Int, total: Int, onClose: () -> Unit) {
    // Bu fonksiyona artık dikey padding vermeye gerek yok, çünkü ana Column onu hallediyor.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp), // Dikey padding biraz artırılabilir.
        verticalAlignment = Alignment.CenterVertically
    ) {
        // İçerik aynı
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
    // Butonun padding'i ana Column'dan geldiği için alt padding'i kontrol edelim.
    Column(modifier = Modifier.fillMaxWidth()) {
        // Geri bildirim barı (Correct/Incorrect) - Değişiklik yok
        if (answerState != AnswerState.UNANSWERED) {
            val (backgroundColor, text, icon) = if (answerState == AnswerState.CORRECT) {
                Triple(lightGreen, "Correct!", Icons.Default.Check)
            } else {
                Triple(lightRed, "Incorrect!", Icons.Default.Close)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = if (answerState == AnswerState.CORRECT) correctGreen else incorrectRed)
                Spacer(Modifier.width(8.dp))
                Text(text, fontWeight = FontWeight.Bold, color = if (answerState == AnswerState.CORRECT) correctGreen else incorrectRed)
            }
        }

        // Buton
        Button(
            onClick = onCheckOrNext,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp), // Alt boşluğu artır
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

// ImageChoiceQuestionUI, MultipleChoiceQuestionUI, TrueFalseQuestionUI, OptionRow ve QuizResultScreen
// fonksiyonları önceki halleriyle aynı kalabilir, içlerinde padding değişikliğine gerek yok.
// Aşağıya bu fonksiyonları tekrar ekliyorum ki dosya tam olsun.

@Composable
fun ImageChoiceQuestionUI(question: QuizItem.ImageChoice, state: com.esma.bunble.presentation.viewmodel.quiz.QuizScreenState, onAnswerSelected: (String) -> Unit) {
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
fun MultipleChoiceQuestionUI(question: QuizItem.MultipleChoice, state: com.esma.bunble.presentation.viewmodel.quiz.QuizScreenState, onAnswerSelected: (String) -> Unit) {
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
fun TrueFalseQuestionUI(question: QuizItem.TrueFalse, state: com.esma.bunble.presentation.viewmodel.quiz.QuizScreenState, onAnswerSelected: (Boolean) -> Unit) {
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
    // 1. Gerekli hesaplamaları yap
    val successRate = if (totalQuestions > 0) {
        (score.toFloat() / totalQuestions.toFloat() * 100).toInt()
    } else {
        0
    }
    val progress = if (totalQuestions > 0) score.toFloat() / totalQuestions.toFloat() else 0f

    // 2. Doğru resmi seç
    val bunnyImageRes = if (successRate >= 80) {
        R.drawable.happy_bunny
    } else {
        R.drawable.sad_bunny
    }

    // 3. Renkleri belirle
    val titleColor = Color(0xFF4A148C) // Koyu Mor
    val successColor = Color(0xFF4CAF50) // Yeşil

    Box(
        modifier = Modifier.fillMaxSize() // Box tüm ekranı kaplar
    ) {
        // --- Arka Plan Resmi ---
        Image(
            painter = painterResource(id = bunnyImageRes),
            contentDescription = "Quiz Result Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Resmi en-boy oranını koruyarak ekranı doldurur
        )

        // --- İçerik Katmanı ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                // --- DEĞİŞİKLİK BURADA ---
                // Sistemin kapladığı alanlar (status bar, navigation bar) kadar
                // içeriğe iç boşluk (padding) ver.
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- ÜST KISIM (Yazılar) ---
            // Üst boşluğu manuel olarak vererek içeriği status bar'dan ayıralım
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

            // --- ORTA KISIM (Boşluk) ---
            Spacer(modifier = Modifier.weight(1f))

            // --- ALT KISIM (İlerleme Çubuğu ve Buton) ---
            // Bu kısım artık otomatik olarak navigation bar'ın üstünde kalacak.
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
            // Alt tarafa da manuel bir boşluk ekleyerek butonun yapışmasını engelleyelim.
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

