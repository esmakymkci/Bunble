package com.esma.bunble.presentation.base.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandYellow

// İstatistik Kartı
@Composable
fun StatsCard(
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
                StatItem(icon = Icons.Default.MilitaryTech, label = "Level 3", value = "120 XP")
                StatItem(icon = Icons.Default.Star, label = "Streak", value = "$streak days")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(icon = Icons.Default.Timer, label = "Total Time", value = timeString)
                StatItem(icon = Icons.Default.MenuBook, label = "Learned", value = "$learnedWords words")
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

/*

RowScope receiver olduğu için bu composable sadece bir Row’un içinde çağırıldığında tam anlamıyla çalışır.

Modifier.weight(1f) RowScope’a özel bir Modifier.weight. Yani: “Bu eleman Row içinde x ağırlıkta yer kaplasın” demek.

Bu Row, bulunduğu üst Row içindeki iki StatItem’dan biri.
Modifier.weight(1f): Aynı üst Row içindeki her StatItem eşit yer kaplasın demek. İki StatItem var → her biri Row’un genişliğinin yarısını alır (padding vs. hariç).

RowScope + weight(1f) sayesinde:
Aynı satırdaki iki StatItem eşit genişlikte ve dengeli dağılıyor.

 */