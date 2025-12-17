package com.esma.bunble.presentation.base.components.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.BrandYellowLighter



// Yatay Kaydırılabilir Butonlar
@Composable
fun ActionButtonsRow(navController : NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(text = "Create List", isPrimary = true, onClick = { navController.navigate("lists_screen") })
        ActionButton(text = "Start Quiz", isPrimary = true, onClick = { navController.navigate("lists_screen") })
        ActionButton(text = "Stories", isPrimary = true, onClick = { navController.navigate("stories_screen") })
        ActionButton(text = "Chat", isPrimary = true, onClick = { navController.navigate("chat_screen") })
    }
}

// Aksiyon Butonu
@Composable
fun ActionButton(text: String, isPrimary: Boolean = false, onClick: () -> Unit = {}) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) BrandYellow else BrandYellowLighter,
            contentColor = BrandBlack
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text = text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
    }
}




/*

horizontalScroll(rememberScrollState()):
Row ekrana sığmazsa, kullanıcı parmağıyla yatay kaydırarak diğer butonları görebilir.

elevation = ButtonDefaults.buttonElevation(0.dp)
Butonun gölge (elevation) değerini ayarlıyor.

 */