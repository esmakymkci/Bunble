package com.esma.bunble.presentation.ui.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.viewmodel.story.EditStoryViewModel
import com.esma.bunble.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStoryScreen(
    navController: NavController,
    viewModel: EditStoryViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    var title by remember(state.story?.title) { mutableStateOf(state.story?.title ?: "") }
    var content by remember(state.story?.content) { mutableStateOf(state.story?.content ?: "") }
    val contentScrollState = rememberScrollState()

    val isButtonEnabled = title.isNotBlank() && content.isNotBlank()

    val veryLightYellow = BrandYellow.copy(alpha = 0.05f)
    val backgroundColor = Color.White

    // Güncelleme başarılı olduğunda geri dön
    LaunchedEffect(key1 = state.updateSuccess) {
        if (state.updateSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.edit_story_title),
                            modifier = Modifier.align(Alignment.Center),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.edit_story_back_button_desc)
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(veryLightYellow)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            stringResource(id = R.string.add_story_title_placeholder),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Gray,
                        unfocusedIndicatorColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(contentScrollState),
                    placeholder = {
                        Text(
                            stringResource(id = R.string.add_story_content_placeholder),
                            fontSize = 16.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    },
                    textStyle = TextStyle(fontSize = 16.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // genel bir hata göster
            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // "Publish" butonu
            Button(
                onClick = {
                    if (isButtonEnabled) {
                        viewModel.updateStory(title, content)
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandYellow,
                    disabledContainerColor = BrandYellow.copy(alpha = 0.5f)
                )
            ) {
                // Yükleme durumuna göre butonun içeriğini değiştir
                if (state.isUpdating) { // 'isAddingStory' yerine 'isUpdating'
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        stringResource(id = R.string.edit_story_update_button),
                        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
