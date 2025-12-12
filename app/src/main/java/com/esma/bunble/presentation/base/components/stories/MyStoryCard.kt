package com.esma.bunble.presentation.base.components.stories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// ▼▼▼ 1. DOĞRU MODELİ IMPORT ET ▼▼▼
import com.esma.bunble.domain.model.UserStory

@Composable
fun MyStoryCard(
    story: UserStory,
    modifier: Modifier = Modifier
) {
    val cardColor = remember(story.id) {
        pastelColors[story.id.hashCode().mod(pastelColors.size)]
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = story.title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = story.content,
                color = Color.Black.copy(alpha = 0.6f),
                fontSize = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp
            )
        }
    }
}

private val pastelColors = listOf(
    Color(0xFFBBDEFB), // Pastel Mavi
    Color(0xFFC8E6C9), // Pastel Yeşil
    Color(0xFFF8BBD0), // Pastel Pembe
    Color(0xFFFFF9C4), // Pastel Sarı
    Color(0xFFD1C4E9), // Pastel Mor
    Color(0xFFFFCCBC), // Pastel Turuncu
    Color(0xFFB2EBF2)  // Pastel Cyan
)

@Preview(showBackground = true)
@Composable
private fun MyStoryCardPreview() {
    val sampleStory = UserStory(
        id = "preview1",
        title = "A Trip to the Market",
        content = "Yesterday, I went to the big market downtown. The colors of the fruits and vegetables were amazing...",
        imageUrl = ""
    )
    Column(modifier = Modifier.padding(16.dp)) {
        MyStoryCard(story = sampleStory)
    }
}

