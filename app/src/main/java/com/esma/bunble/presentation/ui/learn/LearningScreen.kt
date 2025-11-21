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
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.esma.bunble.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    navController: NavController,
    categoryTitle: String?

) {
    // Bu liste normalde ViewModel veya Repository'den gelir.
    // Şimdilik örnek verilerle dolduruyoruz.
    val learningItems = remember {
        listOf(
            LearnItem(
                text = "I would like to order...",
                phonetic = "/aɪ wʊd laɪk tu ˈɔrdər.../",
                meaning = "Used to politely tell staff what you want to eat or drink.",
                translation = "Me gustaría pedir...",
                imageRes = R.drawable.food2
            ),
            LearnItem(
                text = "Check, please.",
                phonetic = "/ʧɛk, pliz/",
                meaning = "A phrase used to ask for the bill in a restaurant.",
                translation = "La cuenta, por favor.",
                imageRes = R.drawable.food3 // Kendi görsellerinizle değiştirin
            )
        )
    }

    var currentItemIndex by remember { mutableIntStateOf(0) }
    val currentItem = learningItems[currentItemIndex]

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = categoryTitle ?: "Learning",
                    fontWeight = FontWeight.Bold
                    ) },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        containerColor = Color(0xFFF7F7F7)
    ) { innerPadding ->
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
                verticalArrangement = Arrangement.Center // İçeriği kendi içinde ortalar
            ) {
                Image(
                    painter = painterResource(id = currentItem.imageRes),
                    contentDescription = currentItem.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.5f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(24.dp))

                Text(text = currentItem.text, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(text = currentItem.phonetic, fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)

                Spacer(Modifier.height(16.dp))

                // Anlam/Açıklama bölümü
                Text(
                    text = currentItem.meaning,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(24.dp))

                // Çeviri Kartı
                TranslationCard(translation = currentItem.translation)
            }



            Button(
                onClick = {
                    if (currentItemIndex < learningItems.size - 1) {
                        currentItemIndex++
                    } else {
                        // Liste bittiğinde yapılacak işlem, örneğin kategori detay sayfasına dön
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDD835))
            ) {
                Text(
                    text = if (currentItemIndex < learningItems.size - 1) "Continue" else "Finish",
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun TranslationCard(translation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Pronounce",
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFFC107).copy(alpha = 0.2f), CircleShape)
                        .padding(8.dp),
                    tint = Color(0xFFD4A000)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(text = translation, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Translation", color = Color.Gray, fontSize = 12.sp)
                }
            }
            IconButton(onClick = { /* TODO: Bookmark action */ }) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = Color.Gray)
            }
        }
    }
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LearningScreenPreview() {
    LearningScreen(rememberNavController(), "Ordering Food")
}
