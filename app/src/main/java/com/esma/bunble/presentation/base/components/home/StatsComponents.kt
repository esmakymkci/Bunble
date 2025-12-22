package com.esma.bunble.presentation.base.components.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource // <-- YENİ IMPORT
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esma.bunble.R // <-- YENİ IMPORT
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandYellow

// İstatistik Kartı
@Composable
fun StatsCard(
    level: Int,
    currentXp: Int,
    streak: Int,
    totalTimeSpentMinutes: Long,
    learnedWords: Int
) {
    // Toplam süreyi "2h 30m" formatına çeviren yardımcı mantık
    val hours = totalTimeSpentMinutes / 60
    val minutes = totalTimeSpentMinutes % 60
    val timeString = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandYellow.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(
                    icon = Icons.Default.MilitaryTech,
                    label = stringResource(id = R.string.stats_level, level),
                    value = stringResource(id = R.string.stats_xp, currentXp.toString())
                )
                StatItem(
                    icon = Icons.Default.Star,
                    label = stringResource(id = R.string.stats_streak),
                    value = stringResource(id = R.string.stats_streak_value, streak)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(
                    icon = Icons.Default.Timer,
                    label = stringResource(id = R.string.stats_total_time),
                    value = timeString // Bu zaten dinamik, string resource'a gerek yok
                )
                StatItem(
                    icon = Icons.Default.MenuBook,
                    label = stringResource(id = R.string.stats_learned),
                    value = stringResource(id = R.string.stats_learned_value, learnedWords)
                )
            }
        }
    }
}

// Tek bir istatistik elemanı
@Composable
fun RowScope.StatItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(imageVector = icon, contentDescription = label, tint = BrandBlack)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = value, color = BrandBlack, fontSize = 14.sp)
        }
    }
}
