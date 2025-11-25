package com.esma.bunble.presentation.ui.language_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.esma.bunble.data.local.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@Composable
fun LanguageSelectionScreen(
    navController: NavController,
    viewModel: LanguageSelectionViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val userPrefsRepo = viewModel.userPrefsRepo

    val deviceLanguageCode = Locale.getDefault().language
    val deviceLanguageName = Locale.getDefault().displayLanguage.replaceFirstChar { it.uppercase() }

    var selectedLanguage by remember { mutableStateOf<Language?>(null) }

    // --- DEĞİŞİKLİK 1: TAM EKRAN TASARIM ---
    // Box kaldırıldı, doğrudan Column kullanılıyor ve padding eklendi.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) // Arka plan artık tam ekran
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Select Your Language", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Your language: $deviceLanguageName", color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))
            Text("I want to learn...", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // LazyColumn artık Column içinde olduğu için bir weight almalı
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(supportedLanguages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = language.code == selectedLanguage?.code,
                    onLanguageSelected = { selectedLanguage = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                selectedLanguage?.let { targetLang ->
                    coroutineScope.launch {
                        userPrefsRepo.saveLanguageSelection(
                            source = deviceLanguageCode,
                            target = targetLang.code
                        )
                        userPrefsRepo.setFirstLaunchCompleted()
                        navController.navigate("signup_screen") {
                            popUpTo("splash_screen") { inclusive = true }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDD835)),
            enabled = selectedLanguage != null
        ) {
            Text("Confirm Selection", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LanguageItem(language: Language, isSelected: Boolean, onLanguageSelected: (Language) -> Unit) {
    // --- DEĞİŞİKLİK 2: YENİ SEÇİM STİLİ ---
    val brandYellow = Color(0xFFFDD835)
    val lightYellowTransparent = brandYellow.copy(alpha = 0.2f)

    // Seçim durumuna göre arka plan ve kenarlık rengini belirle
    val backgroundColor = if (isSelected) lightYellowTransparent else Color.Transparent
    val borderColor = if (isSelected) brandYellow else Color.LightGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(2.dp, borderColor, RoundedCornerShape(50))
            .background(backgroundColor, RoundedCornerShape(50)) // Arka planı uygula
            .clip(RoundedCornerShape(50)) // Kenarları yuvarlat
            .clickable { onLanguageSelected(language) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(language.flagEmoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(language.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        RadioButton(
            selected = isSelected,
            onClick = { onLanguageSelected(language) },
            colors = RadioButtonDefaults.colors(selectedColor = brandYellow)
        )
    }
}

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    val userPrefsRepo: UserPreferencesRepository
) : ViewModel()
