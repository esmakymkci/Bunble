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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.esma.bunble.R
import com.esma.bunble.presentation.base.components.category_detail.DetailSection
import com.esma.bunble.presentation.viewmodel.category_detail.CategoryDetailViewModel

@Composable
fun CategoryDetailScreen(
    navController: NavController,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    // ViewModel'den gelen state'i dinle
    val state = viewModel.state.value

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
    ) { innerPadding ->
        // 1. Yüklenme Durumu Kontrolü
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        // 2. Hata Durumu Kontrolü
        else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${state.error}")
            }
        }
        // 3. Başarılı Veri Durumu
        else if (state.category != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Başlık ve resim için item
                item {
                    // CategoryHeader'ı yeni state yapısıyla kullan
                    CategoryHeader(
                        // HATA DÜZELTME: state.title yerine state.category.name
                        title = state.category.name,
                        // HATA DÜZELTME: state.imageUrl yerine state.category.imageUrl
                        imageUrl = state.category.imageUrl,
                        onBackClicked = { navController.popBackStack() }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sık Kullanılan İfadeler bölümü
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

                // Sık Kullanılan Kelimeler bölümü
                item {
                    DetailSection(
                        title = "Frequently Used Phrases",
                        cardTitle = "Common Phrases",
                        cardDescription = "Learn essential phrases for this topic.",
                        buttonText = "View",
                        imageRes = R.drawable.word,
                        onButtonClick = {
                            navController.navigate("learning_screen/${state.category.id}")

                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Quiz bölümü
                item {
                    DetailSection(
                        title = "Quiz",
                        cardTitle = "Dialogue Quiz",
                        cardDescription = "Test your knowledge with a dialogue-based quiz.",
                        buttonText = "Start Quiz",
                        imageRes = R.drawable.quiz1,
                        onButtonClick = {
                            // Quiz ekranına, mevcut kategori ID'si ile git.
                            navController.navigate("quiz_screen/${state.category.id}")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// CategoryHeader Composable'ı doğru, bir değişiklik gerekmiyor.
@Composable
fun CategoryHeader(
    title: String,
    imageUrl: String,
    onBackClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageUrl),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun CategoryDetailScreenPreview() {
    // Preview'u, ekranın tamamı yerine bileşen bazlı yapmak daha sağlıklıdır.
    CategoryHeader(
        title = "Ordering at a Restaurant",
        imageUrl = "", // Preview için boş bırakılabilir
        onBackClicked = {}
    )
}
