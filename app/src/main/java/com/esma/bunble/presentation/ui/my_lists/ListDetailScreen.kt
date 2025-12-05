package com.esma.bunble.presentation.ui.my_lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.domain.model.Word
import com.esma.bunble.domain.model.WordList
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.GrayText
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.my_lists.ListDetailViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    navController: NavController,
    viewModel: ListDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var textToAdd by remember { mutableStateOf("") }

    val filteredWords = remember(searchQuery, uiState.words) {
        if (searchQuery.isBlank()) {
            uiState.words
        } else {
            uiState.words.filter {
                it.sourceText.contains(searchQuery, ignoreCase = true) ||
                        it.translatedText.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            ListDetailTopBar(
                title = uiState.listDetails?.title ?: "...",
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onToggleSearch = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) searchQuery = "" // Aramayı kapatınca temizle
                },
                onNavigateUp = { navController.navigateUp() }
            )
        },
        containerColor = SurfaceLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // İlerleme Çubuğu ve Diller
            uiState.listDetails?.let { list ->
                Spacer(modifier = Modifier.height(8.dp))
                ProgressSection(list = list)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Yeni Kelime Ekleme Alanı
            AddWordInput(
                text = textToAdd,
                onTextChange = { textToAdd = it },
                onAddClick = {
                    if (textToAdd.isNotBlank()) {
                        viewModel.addWord(textToAdd)
                        textToAdd = "" // Ekleme sonrası metni temizle
                    }
                },
                isLoading = uiState.isTranslating
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Kelime Listesi
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandYellow)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = Color.Red, textAlign = TextAlign.Center)
                }
            }
            else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(items = filteredWords, key = { it.id }) { word ->
                        NewWordItemCard(
                            word = word,
                            onUpdateLearnedStatus = { viewModel.updateWordLearnedStatus(it) },
                            onDelete = { viewModel.deleteWord(it) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailTopBar(
    title: String,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onNavigateUp: () -> Unit
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        title = {
            AnimatedVisibility(visible = !isSearchActive, enter = fadeIn(), exit = fadeOut()) {
                Text(title, fontWeight = FontWeight.Bold)
            }
            AnimatedVisibility(visible = isSearchActive, enter = fadeIn(), exit = fadeOut()) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search words...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = BrandYellow,
                        cursorColor = BrandYellow
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SurfaceLight)
    )
}

@Composable
fun ProgressSection(list: WordList) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(findLanguageByCode(list.sourceLang)?.flagEmoji ?: "🏴", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("→", color = GrayText, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(findLanguageByCode(list.targetLang)?.flagEmoji ?: "🏴", fontSize = 24.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Progress", style = MaterialTheme.typography.bodySmall, color = GrayText)
            Text(
                "${list.learnedCount} / ${list.wordCount} learned",
                style = MaterialTheme.typography.bodySmall,
                color = GrayText,
                fontWeight = FontWeight.SemiBold
            )
        }
        LinearProgressIndicator(
            progress = { if (list.wordCount > 0) list.learnedCount.toFloat() / list.wordCount.toFloat() else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = BrandYellow,
            trackColor = BrandBlack.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun AddWordInput(
    text: String,
    onTextChange: (String) -> Unit,
    onAddClick: () -> Unit,
    isLoading: Boolean
) {
    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 16.sp),
        cursorBrush = SolidColor(BrandYellow),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (text.isEmpty()) {
                        Text("Add a new word...", color = Color.LightGray, fontSize = 16.sp)
                    }
                    innerTextField()
                }
                IconButton(
                    onClick = onAddClick,
                    enabled = !isLoading,
                    modifier = Modifier
                        .size(32.dp)
                        .background(BrandYellow, CircleShape)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                    } else {
                        Icon(Icons.Default.Add, contentDescription = "Add Word", tint = Color.Black)
                    }
                }
            }
        }
    )
}


@Composable
fun NewWordItemCard(
    word: Word,
    onUpdateLearnedStatus: (Word) -> Unit,
    onDelete : (Word) -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) } // Menü durumunu tutar

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Üst Satır: Kaynak Metin ve Aksiyonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = word.sourceText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f) // İkonların sağa yaslanması için
                )

                // Aksiyon İkonları
                IconButton(onClick = { onUpdateLearnedStatus(word) }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Mark as learned",
                        tint = if (word.isLearned) BrandYellow else Color.LightGray
                    )
                }
                IconButton(onClick = { /* TODO: TTS (Text-to-Speech) */ }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = Color.Gray)
                }
                Box { // DropdownMenu'yü doğru konumlandırmak için Box kullan
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.Gray)
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        // DÜZENLE SEÇENEĞİ
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                // TODO: Düzenleme ekranına git veya bir dialog aç
                                menuExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        )
                        // SİLME SEÇENEĞİ
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.Red) },
                            onClick = {
                                onDelete(word) // ViewModel'deki fonksiyonu çağır
                                menuExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        )
                    }
                }
            }

            // Orta Satır: Çeviri ve Fonetik
            Column {
                Text(
                    text = word.translatedText,
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandYellow, // Tema rengin
                    fontWeight = FontWeight.SemiBold
                )

                if (!word.phonetic.isNullOrBlank()) {
                    Text(
                        text = "/${word.phonetic}/",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }


            // Alt Bölüm: Örnek Cümleler
            if (word.examples.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 6.dp)) // Ayırıcı çizgi
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    word.examples.forEach { example ->
                        Text(
                            text = "• $example",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}






