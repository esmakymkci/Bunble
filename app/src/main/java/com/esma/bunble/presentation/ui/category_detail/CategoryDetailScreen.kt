package com.esma.bunble.presentation.ui.category_detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.esma.bunble.R
import com.esma.bunble.presentation.base.components.category_detail.DetailSection
import com.esma.bunble.presentation.base.components.category_detail.HeaderSection
import com.esma.bunble.presentation.viewmodel.category_detail.CategoryDetailViewModel

@Composable
fun CategoryDetailScreen(
    navController: NavController,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${state.error}")
            }
        }
        else if (state.category != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    HeaderSection(
                        title = state.category.name,
                        imageUrl = state.category.imageUrl,
                        onBackClicked = { navController.popBackStack() }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    DetailSection(
                        title = "Frequently Used Words",
                        cardTitle = "Common Words",
                        cardDescription = "Learn essential words for this topic.",
                        buttonText = "View",
                        imageRes = R.drawable.sentence1, // Şimdilik statik kalabilir
                        onButtonClick = {
                            navController.navigate("learning_screen/${state.category.id}?type=words")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    DetailSection(
                        title = "Frequently Used Phrases",
                        cardTitle = "Common Phrases",
                        cardDescription = "Learn essential phrases for this topic.",
                        buttonText = "View",
                        imageRes = R.drawable.word,
                        onButtonClick = {
                            navController.navigate("learning_screen/${state.category.id}?type=phrases")

                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    DetailSection(
                        title = "Quiz",
                        cardTitle = "Dialogue Quiz",
                        cardDescription = "Test your knowledge with a dialogue-based quiz.",
                        buttonText = "Start Quiz",
                        imageRes = R.drawable.quiz1,
                        onButtonClick = {
                            navController.navigate("learning_screen/${state.category.id}?type=quiz")                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}



