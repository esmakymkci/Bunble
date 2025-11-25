package com.esma.bunble.presentation.ui.learn

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.esma.bunble.presentation.viewmodel.learn.LearnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    navController: NavController,
    viewModel: LearnViewModel = hiltViewModel()
) {
    // 1. Sadece tek bir state objesini dinle.
    val state = viewModel.state.value
    // Mevcut öğrenme kartını state'ten al. Bu yapı null güvenlidir.
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
        // 2. Yüklenme, Hata ve Veri durumlarını yönet.
        when {
            // Yüklenme durumu
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Hata durumu
            state.error != null -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.error}", textAlign = TextAlign.Center)
                }
            }
            // Başarılı veri durumu ama içerik boş
            state.items.isEmpty() -> {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Bu kategoride öğrenilecek içerik bulunamadı.", textAlign = TextAlign.Center)
                }
            }
            // Başarılı veri ve gösterilecek kart var
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

                        // 3. Verileri `currentItem` üzerinden güvenle göster.
                        Text(text = currentItem.text, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(text = currentItem.phonetic, fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = currentItem.translation, // 'meaning' yerine ana çeviriyi gösterelim.
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(Modifier.height(24.dp))

                        // 'TranslationCard' için ana dilimizdeki çeviriyi kullanalım.
                        // 'meaning' alanı, kelimenin/ifadenin daha detaylı açıklaması olabilir.
                        // Şimdilik phonetic kullanıyorum, sen modeline göre değiştirebilirsin.
                        TranslationCard(
                            translation = currentItem.phonetic,
                            audioUrl = currentItem.audioUrl,
                            onPlayAudio = { viewModel.playAudio(currentItem.audioUrl) }
                        )
                    }

                    // 4. Butonun mantığını state üzerinden yönet.
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
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

// Bu alt bileşende değişiklik yok, doğru çalışıyor.
@Composable
fun TranslationCard(
    translation: String,
    audioUrl: String?,
    onPlayAudio: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
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
                    onClick = onPlayAudio, // 2. Tıklama olayını buraya taşı
                    enabled = !audioUrl.isNullOrBlank() // 3. 'enabled' durumunu buraya taşı
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Pronounce",
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFC107).copy(alpha = 0.2f), CircleShape)
                            .padding(8.dp),
                        // 4. Renk değiştirme mantığını buraya taşı
                        tint = if (!audioUrl.isNullOrBlank()) Color(0xFFD4A000) else Color.Gray
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = translation, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Phonetic", color = Color.Gray, fontSize = 12.sp) // Burayı anlama göre güncelledim
                }
            }
            IconButton(
                onClick = {}
            ) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LearningScreenPreview() {
    Text("Preview is disabled for screens using Hilt ViewModel.")
}
