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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.esma.bunble.data.local.UserPreferencesRepository
import com.esma.bunble.presentation.theme.ui.BorderGray
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.BrandYellowTransparent
import com.esma.bunble.presentation.theme.ui.GrayText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import com.esma.bunble.R


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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(id = R.string.lang_selection_title), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(id = R.string.lang_selection_your_language, deviceLanguageName), color = GrayText)
            Spacer(modifier = Modifier.height(32.dp))
            Text(stringResource(id = R.string.lang_selection_i_want_to_learn), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

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
            colors = ButtonDefaults.buttonColors(containerColor = BrandYellow),
            enabled = selectedLanguage != null
        ) {
            Text(stringResource(id = R.string.lang_selection_button_confirm), color = BrandBlack, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LanguageItem(language: Language, isSelected: Boolean, onLanguageSelected: (Language) -> Unit) {

    val backgroundColor = if (isSelected) BrandYellowTransparent else Color.Transparent
    val borderColor = if (isSelected) BrandYellow else BorderGray

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
            colors = RadioButtonDefaults.colors(selectedColor = BrandYellow)
        )
    }
}

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    val userPrefsRepo: UserPreferencesRepository
) : ViewModel()
