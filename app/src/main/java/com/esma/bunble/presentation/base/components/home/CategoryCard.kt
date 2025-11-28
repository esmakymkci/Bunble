package com.esma.bunble.presentation.base.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandWhite



// Tek bir kategori kartı
@Composable
fun CategoryCard(title: String, imageUrl: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Üstteki siyah şerit
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(BrandBlack.copy(alpha = 0.3f), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(text = title, color = BrandWhite, fontWeight = FontWeight.Bold)
                    Text(text = "${(20..80).random()}% complete", color = BrandWhite.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }
    }
}

/*
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoriesGrid(categories: List<Pair<String, Int>>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories) { (title, imageRes) ->
            CategoryCard(title = title, imageRes = imageRes)
        }
    }
}

*/


/*

modifier = Modifier.aspectRatio(1f):
Kartın genişlik : yükseklik oranını 1:1 yapar. Yani kare kart: hangi alana yerleşirse yerleşsin hep kare kalır

contentScale = ContentScale.Crop:
Görsel tam sığmıyorsa. Oranını bozmaz, Ama kartı doldurmak için gerekirse kırpar (crop).



 */