package com.esma.bunble.presentation.ui.category_detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.R
import com.esma.bunble.presentation.base.components.category_detail.DetailSection
import com.esma.bunble.presentation.base.components.category_detail.HeaderSection
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.category_detail.CategoryDetailViewModel

@Composable
fun CategoryDetailScreen(
    navController: NavController,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        containerColor = SurfaceLight,
    ) { innerPadding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(id = state.error))
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
                        title = stringResource(id = R.string.detail_section_frequent_words),
                        cardTitle = stringResource(id = R.string.detail_card_common_words),
                        cardDescription = stringResource(id = R.string.detail_card_common_words_desc),
                        buttonText = stringResource(id = R.string.common_view),
                        imageRes = R.drawable.sentence1,
                        onButtonClick = {
                            navController.navigate("learning_screen/${state.category!!.id}?type=words")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    DetailSection(
                        title = stringResource(id = R.string.detail_section_frequent_phrases),
                        cardTitle = stringResource(id = R.string.detail_card_common_phrases),
                        cardDescription = stringResource(id = R.string.detail_card_common_phrases_desc),
                        buttonText = stringResource(id = R.string.common_view),
                        imageRes = R.drawable.word,
                        onButtonClick = {
                            navController.navigate("learning_screen/${state.category!!.id}?type=phrases")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    DetailSection(
                        title = stringResource(id = R.string.detail_section_quiz),
                        cardTitle = stringResource(id = R.string.detail_card_dialogue_quiz),
                        cardDescription = stringResource(id = R.string.detail_card_dialogue_quiz_desc),
                        buttonText = stringResource(id = R.string.common_start_quiz),
                        imageRes = R.drawable.quiz1,
                        onButtonClick = {
                            navController.navigate("learning_screen/${state.category!!.id}?type=quiz")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}



