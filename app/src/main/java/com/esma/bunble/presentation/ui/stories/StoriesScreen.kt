package com.esma.bunble.presentation.ui.stories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.domain.model.PublicStory
import com.esma.bunble.domain.model.StoryListItem
import com.esma.bunble.domain.model.UserStory
import com.esma.bunble.presentation.base.components.home.AppBottomBar
import com.esma.bunble.presentation.base.components.my_lists.SwipeToDeleteContainer
import com.esma.bunble.presentation.base.components.stories.DifficultyChip
import com.esma.bunble.presentation.base.components.stories.MyStoryCard
import com.esma.bunble.presentation.base.components.stories.StoryCard
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.GrayText
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.story.StoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoriesScreen(
    navController: NavController,
    viewModel: StoriesViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    var storyToDelete by remember { mutableStateOf<String?>(null) }
    val showDeleteDialog = storyToDelete != null


    val difficulties = listOf("All", "Beginner", "Intermediate", "Difficult")
    var selectedDifficulty by remember { mutableStateOf("All") }

    // Floating Action Button'un görünürlüğünü kontrol eder
    val isFabVisible = state.selectedTabIndex == 1 // 1: User Tab

    val filteredStories: List<StoryListItem> = remember(
        state.selectedTabIndex,
        selectedDifficulty,
        state.publicStories,
        state.userStories
    ) {
        when (state.selectedTabIndex) {
            0 -> { // 0: Public Tab
                if (selectedDifficulty == "All") {
                    state.publicStories
                } else {
                    state.publicStories.filter { it.difficulty == selectedDifficulty }
                }
            }
            1 -> state.userStories // 1: User Tab
            else -> emptyList()
        }
    }

    //  POPUP
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { storyToDelete = null },
            title = { Text("Delete Story") },
            text = { Text("Are you sure you want to permanently delete this story?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        storyToDelete?.let { viewModel.deleteUserStory(it) }
                        storyToDelete = null
                    }
                ) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { storyToDelete = null }) { Text("Cancel") }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.refreshStories()
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
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
        },
        floatingActionButton = {
            AnimatedVisibility(visible =  isFabVisible){
                FloatingActionButton(
                    onClick = {
                        navController.navigate("add_story_screen")
                    },
                    containerColor = BrandYellow,
                    contentColor = BrandBlack
                ){
                    Icon(Icons.Default.Add, contentDescription = "Add Story")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp)
                    .background(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                // Hareket eden sarı bar (Indicator)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight()
                        .padding(4.dp)
                        .align(if (state.selectedTabIndex == 0) Alignment.CenterStart else Alignment.CenterEnd)
                        .background(
                            color = BrandYellow,
                            shape = RoundedCornerShape(50)
                        )
                )
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // "Stories" Sekmesi
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .clickable(
                                onClick = { viewModel.onTabSelected(0) },
                                indication = null, // Tıklama efektini kaldır
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stories",
                            fontWeight = FontWeight.Bold,
                            color = if (state.selectedTabIndex == 0) BrandBlack else GrayText
                        )
                    }
                    // "My Stories" Sekmesi
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .clickable(
                                onClick = { viewModel.onTabSelected(1) },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "My Stories",
                            fontWeight = FontWeight.Bold,
                            color = if (state.selectedTabIndex == 1) BrandBlack else GrayText
                        )
                    }
                }
            }

            if (state.isLoading && filteredStories.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = BrandYellow)
                }
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.error}", color = Color.Red)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sadece genel hikayeler sekmesindeyken zorluk filtrelerini göster
                    if (state.selectedTabIndex == 0) {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
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
                    }
                    // Hikaye listesi
                    items(filteredStories, key = { it.id }) { story ->
                        when (story) {
                            is PublicStory -> {
                                StoryCard(
                                    story = story,
                                    modifier = Modifier.clickable {
                                        val encodedTitle = java.net.URLEncoder.encode(story.title, "UTF-8")
                                        navController.navigate("story_detail_screen/${story.id}?title=$encodedTitle")                                    }
                                )
                            }
                            is UserStory -> {
                                SwipeToDeleteContainer(
                                    onDelete = {
                                        storyToDelete = story.id
                                    }
                                ) {
                                    MyStoryCard(
                                        story = story,
                                        modifier = Modifier.clickable {
                                            navController.navigate("story_detail_screen/${story.id}")                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (state.selectedTabIndex == 1 && filteredStories.isEmpty() && !state.isLoading) {                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 100.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "You haven't added any stories yet.",
                                    fontWeight = FontWeight.Medium
                                )
                                Text("Tap the '+' button to add your first story!")
                            }
                        }
                    }
                }
            }

        }
    }
}
