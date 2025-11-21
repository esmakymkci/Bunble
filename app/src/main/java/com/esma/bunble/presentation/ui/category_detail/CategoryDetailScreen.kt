package com.esma.bunble.presentation.ui.category_detail


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.esma.bunble.R
import com.esma.bunble.presentation.base.components.category_detail.DetailSection
import com.esma.bunble.presentation.base.components.category_detail.HeaderSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryTitle: String?,
    categoryImageRes: Int?,
    navController: NavController
) {
    Scaffold(
        containerColor = Color(0xFFF7F7F7),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                HeaderSection(
                    title = categoryTitle ?: "Category",
                    imageRes = categoryImageRes ?: R.drawable.food2,
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
                    imageRes = R.drawable.word,
                    onButtonClick = {
                        categoryTitle?.let {
                            navController.navigate("learning_screen/$it")
                        }
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
                    imageRes = R.drawable.sentence1,
                    onButtonClick = {
                        categoryTitle?.let {
                            navController.navigate("learning_screen/$it")
                        }
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

                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}



@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun CategoryDetailScreenPreview() {
    CategoryDetailScreen(
        categoryTitle = "Ordering at a Restaurant",
        categoryImageRes = R.drawable.food2, // Örnek bir resim
        navController = rememberNavController()
    )
}


