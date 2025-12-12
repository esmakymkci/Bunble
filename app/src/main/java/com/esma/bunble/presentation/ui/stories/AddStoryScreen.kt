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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.viewmodel.story.StoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryScreen(
    navController: NavController,
    viewModel: StoriesViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val contentScrollState = rememberScrollState()

    val addStoryState = viewModel.state.value
    val isButtonEnabled = title.isNotBlank() && content.isNotBlank()

    val veryLightYellow = BrandYellow.copy(alpha = 0.05f)
    val backgroundColor = Color.White

    // BAŞARILI EKLEME İŞLEMİNDEN SONRA OTOMATİK GERİ DÖN
    LaunchedEffect(key1 = addStoryState.addStorySuccess) {
        if (addStoryState.addStorySuccess) {
            navController.popBackStack()
            // State'i sıfırla tekrar tetiklenmemesi için
            viewModel.resetAddStoryState()
        }
    }

    // Bu, kullanıcı geri tuşuyla çıkarsa da state'in temizlenmesini sağlar.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetAddStoryState()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Create My Story",
                            modifier = Modifier.align(Alignment.Center),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            "Give your story a title...",
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
                            "Once upon a time...",
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

            if (addStoryState.addStoryError != null) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.resetAddStoryState()
                    },
                    title = {
                        Text(
                            text = "Incorrect Language",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    // Popup içeriği
                    text = {
                        Text(text = addStoryState.addStoryError)
                    },
                    // Popup onay butonu
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.resetAddStoryState()
                            }
                        ) {
                            Text("OK", color = BrandYellow, fontWeight = FontWeight.Bold)
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // "Publish" butonu
            Button(
                onClick = {
                    if (isButtonEnabled) {
                        viewModel.addUserStory(title, content)
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
                if (addStoryState.isAddingStory) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Publish", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }            }
        }
    }
}
