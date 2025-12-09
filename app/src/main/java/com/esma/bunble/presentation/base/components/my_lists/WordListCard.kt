package com.esma.bunble.presentation.base.components.my_lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esma.bunble.domain.model.WordList
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandDarkGray
import com.esma.bunble.presentation.theme.ui.BrandWhite
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.GrayText
import com.esma.bunble.util.findLanguageByCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListCard(
    list: WordList,
    onClick: () -> Unit
) {
    val cardBackgroundColor = Color(0xFFFDFBF2)
    val progressBarColor = BrandYellow

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Başlık ve Bayraklar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(list.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(findLanguageByCode(list.sourceLang)?.flagEmoji ?: "🏴", fontSize = 16.sp)
                    Text("→", color = GrayText)
                    Text(findLanguageByCode(list.targetLang)?.flagEmoji ?: "🏴", fontSize = 16.sp)
                }
            }

            // Kelime Sayısı ve İlerleme
            Column {
                Text(
                    "${list.wordCount} words • ${list.completionPercentage}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayText
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { list.completionPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressBarColor,
                    trackColor = BrandDarkGray.copy(alpha = 0.2f)
                )
            }

            //  Quiz Butonu ve Yönlendirme İkonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* TODO: Quiz ekranına yönlendir */ },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandWhite,
                        contentColor = BrandBlack
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp) ,
                    modifier = Modifier.defaultMinSize(minHeight = 1.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = "Quiz",
                        tint = progressBarColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quiz", fontWeight = FontWeight.SemiBold, color = Color.Black)
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Go to list",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}