package com.esma.bunble.presentation.ui.stories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.presentation.base.components.home.AppBottomBar
import com.esma.bunble.presentation.base.components.stories.DifficultyChip
import com.esma.bunble.presentation.base.components.stories.StoryCard
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.story.StoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    navController: NavController,
    viewModel: StoriesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    val difficulties = listOf("All", "Beginner", "Intermediate", "Difficult")
    var selectedDifficulty by remember { mutableStateOf("All") }

    val filteredStories = remember(selectedDifficulty, state.stories) {
        if (selectedDifficulty == "All") {
            state.stories
        } else {
            state.stories.filter { it.difficulty == selectedDifficulty }
        }
    }

    Scaffold(
        containerColor = SurfaceLight,
        bottomBar = { AppBottomBar(navController = navController) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Stories",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,

                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLight
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp) // Dikey boşluk
                ) {
                    items(difficulties) { difficulty ->
                        DifficultyChip(
                            text = difficulty,
                            isSelected = selectedDifficulty == difficulty,
                            onClick = { selectedDifficulty = difficulty }
                        )
                    }
                }
            }
            items(filteredStories) { story ->
                StoryCard(
                    story = story,
                    onClick = {
                        navController.navigate("story_detail_screen/${story.id}")
                    }
                )
            }

        }
    }
}
