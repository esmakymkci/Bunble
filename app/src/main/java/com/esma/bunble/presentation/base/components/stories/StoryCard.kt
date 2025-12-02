package com.esma.bunble.presentation.base.components.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.esma.bunble.domain.model.Story
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandWhite

@Composable
fun StoryCard(
    story: Story,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        // Arka plan resmi
        AsyncImage(
            model = story.imageUrl,
            contentDescription = story.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Resmin üzerine siyah bir gradient ekleyerek metinlerin okunurluğunu artırır
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BrandBlack.copy(alpha = 0.8f)),
                        startY = 300f // Gradient'in başlangıç pozisyonu
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom, // Metinleri aşağıya hizala
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = story.difficulty.uppercase(),
                color = BrandWhite.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = story.title,
                color = BrandWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
