package com.esma.bunble.presentation.ui.my_lists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.my_lists.WordListsViewModel
import com.esma.bunble.util.Language
import com.esma.bunble.util.supportedLanguages
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListScreen(
    navController: NavController,
    viewModel: WordListsViewModel = hiltViewModel()
) {
    // State'ler
    var title by remember { mutableStateOf("") }
    var sourceLanguage by remember { mutableStateOf(supportedLanguages.first { it.code == "tr" }) }
    var targetLanguage by remember { mutableStateOf(supportedLanguages.first { it.code == "en" }) }

    // BottomSheet State'leri
    val languageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isLanguageSheetVisible by remember { mutableStateOf(false) }
    var languageSheetTarget by remember { mutableStateOf<((Language) -> Unit)?>(null) }
    val scope = rememberCoroutineScope()

    val isButtonEnabled by remember { derivedStateOf { title.isNotBlank() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Word List") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = SurfaceLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Ana içerik
            Column(
                modifier = Modifier.weight(1f), // Butonların altta kalması için
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Başlık Alanı
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("List Title") },
                    placeholder = { Text("Enter a title for your word list") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow)
                )

                // Dil Seçiciler
                Text("Languages", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                LanguageSelectorRow(
                    label = "Source Language",
                    selectedLanguage = sourceLanguage,
                    onClick = {
                        languageSheetTarget = { lang -> sourceLanguage = lang }
                        isLanguageSheetVisible = true
                    }
                )
                LanguageSelectorRow(
                    label = "Target Language",
                    selectedLanguage = targetLanguage,
                    onClick = {
                        languageSheetTarget = { lang -> targetLanguage = lang }
                        isLanguageSheetVisible = true
                    }
                )
            }

            // Butonlar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = { navController.navigateUp() }, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        viewModel.createList(title, sourceLanguage.code, targetLanguage.code)
                        navController.navigateUp() // Geri dön
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandYellow,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Text("Create")
                }
            }
        }
    }

    // Alttan Açılan Dil Seçim BottomSheet'i
    if (isLanguageSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isLanguageSheetVisible = false },
            sheetState = languageSheetState,
            containerColor = SurfaceLight
        ) {
            val currentLang = if (languageSheetTarget.toString().contains("source")) sourceLanguage else targetLanguage
            LanguageSelectorSheetContent(
                currentlySelectedLanguage = currentLang,
                onLanguageSelected = { lang ->
                    languageSheetTarget?.invoke(lang)
                    scope.launch { languageSheetState.hide() }.invokeOnCompletion {
                        isLanguageSheetVisible = false
                    }
                },
                onDismiss = {
                    scope.launch { languageSheetState.hide() }.invokeOnCompletion {
                        isLanguageSheetVisible = false
                    }
                }
            )
        }
    }
}


@Composable
private fun LanguageSelectorRow(
    label: String,
    selectedLanguage: Language,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = selectedLanguage.flagEmoji, fontSize = 22.sp)
                Text(selectedLanguage.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
        }
        Icon(Icons.Default.ExpandMore, contentDescription = "Select Language")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageSelectorSheetContent(
    currentlySelectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismiss: () -> Unit
) {

    var searchQuery by remember { mutableStateOf("") }
    val filteredLanguages = remember(searchQuery) {
        supportedLanguages.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Select Language", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            placeholder = { Text("Search languages...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandYellow)
        )

        LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)) {
            items(items = filteredLanguages, key = { it.code }) { lang ->
                val isSelected = lang.code == currentlySelectedLanguage.code
                val backgroundColor = if (isSelected) BrandYellow.copy(alpha = 0.2f) else Color.Transparent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .clickable { onLanguageSelected(lang) }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = lang.flagEmoji, fontSize = 24.sp)
                    Text(lang.name, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
