package com.esma.bunble.presentation.ui.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandWhite
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.BrandYellowLighter
import com.esma.bunble.presentation.theme.ui.GrayText
import com.esma.bunble.presentation.viewmodel.chat.ConversationState
import com.esma.bunble.presentation.viewmodel.chat.ConversationViewModel
import com.esma.bunble.presentation.viewmodel.chat.Language


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    navController: NavController,
    viewModel: ConversationViewModel = viewModel(
        navController.getBackStackEntry("main_graph")
    )
) {

    val state by viewModel.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.startListening()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Conversation Mode", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BrandBlack) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("chat_screen") {
                            // Home'a kadar olan tüm geri yığınını temizle ve Home'u da dahil et.
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            // ChatScreen'in tek bir örneği olmasını sağla.
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Chat", tint = BrandBlack)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // launchSingleTop, eğer zaten ChatScreen en üstteyse yenisini açmaz.
                        navController.navigate("chat_screen") {
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Switch to Chat Mode",
                            tint = BrandBlack
                        )
                    }
                },
                // Şeffaf arka plan
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        // MİKROFONU SABİTLEMEK İÇİN FLOATING ACTION BUTTON
        floatingActionButton = {
            MicButton(
                isListening = state.isListening,
                onClick = {
                    if(state.isListening) {
                        viewModel.stopListening()
                    } else {
                        // İzin iste ve dinlemeyi başlat
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = BrandWhite
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Dil Seçim Alanı
            LanguageSelector(
                state = state,
                onSourceMenuToggle = { isVisible -> viewModel.onSourceMenuToggle(isVisible) },
                onTargetMenuToggle = { isVisible -> viewModel.onTargetMenuToggle(isVisible) },
                onSwapLanguages = { viewModel.onSwapLanguages() },
                onSourceLanguageSelected = { viewModel.onSourceLanguageSelected(it) },
                onTargetLanguageSelected = { viewModel.onTargetLanguageSelected(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // İçerik Alanı
            TranslationContent(
                state = state,
                modifier = Modifier.weight(1f),
                onListenClicked = { viewModel.speakTranslatedText() },
                onCopyClicked = {  },
                onSaveClicked = {  }
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    state: ConversationState,
    onSourceMenuToggle: (Boolean) -> Unit,
    onTargetMenuToggle: (Boolean) -> Unit,
    onSwapLanguages: () -> Unit,
    onSourceLanguageSelected: (Language) -> Unit,
    onTargetLanguageSelected: (Language) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Kaynak Dil Dropdown
        LanguageDropDown(
            language = state.sourceLanguage,
            isOpen = state.isSourceMenuVisible,
            onClick = { onSourceMenuToggle(true) },
            onDismiss = { onSourceMenuToggle(false) },
            onSelect = onSourceLanguageSelected,
            availableLanguages = state.availableLanguages
        )
        // Dilleri Değiştir Butonu
        IconButton(onClick = onSwapLanguages) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swap Languages",
                tint = BrandYellow
            )
        }

        // Hedef Dil Dropdown
        LanguageDropDown(
            language = state.targetLanguage,
            isOpen = state.isTargetMenuVisible,
            onClick = { onTargetMenuToggle(true) },
            onDismiss = { onTargetMenuToggle(false) },
            onSelect = onTargetLanguageSelected,
            availableLanguages = state.availableLanguages
        )
    }
}

@Composable
private fun LanguageDropDown(
    language: Language,
    isOpen: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (Language) -> Unit,
    availableLanguages: List<Language>
) {
    Box {
        OutlinedButton(onClick = onClick, border = BorderStroke(1.dp, BrandYellow)) {
            Text(language.displayName, color = BrandBlack)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BrandBlack)
        }
        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = onDismiss
        ) {
            availableLanguages.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.displayName) },
                    onClick = { onSelect(lang) }
                )
            }
        }
    }
}

@Composable
private fun TranslationContent(
    state: ConversationState,
    modifier: Modifier = Modifier,
    onListenClicked: () -> Unit,
    onCopyClicked: () -> Unit,
    onSaveClicked: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Tanınan Metin
        AnimatedVisibility(visible = state.recognizedText.isNotBlank()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("YOU SAID", fontSize = 12.sp, color = BrandYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.recognizedText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlack
                )
                Spacer(modifier = Modifier.height(24.dp))
                Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = GrayText)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        // Çevrilen Metin Kutusu
        AnimatedVisibility(visible = state.translatedText.isNotBlank() || state.isTranslating) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandYellowLighter)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${state.targetLanguage.displayName.uppercase()} TRANSLATION",
                        fontSize = 12.sp,
                        color = BrandYellow,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    } else {
                        Text(
                            text = state.translatedText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlack
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Dinle, Kopyala, Kaydet Butonları
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ActionButton(icon = Icons.Default.VolumeUp, text = "Listen", onClick = onListenClicked)
                        ActionButton(icon = Icons.Default.ContentCopy, text = "Copy", onClick = {
                            clipboardManager.setText(AnnotatedString(state.translatedText))
                        })
                        ActionButton(icon = Icons.Default.BookmarkBorder, text = "Save", onClick = onSaveClicked)
                    }
                }
            }
        }
    }
}


@Composable
private fun ActionButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = text, tint = GrayText)
        Text(text, fontSize = 12.sp, color = GrayText)
    }
}

@Composable
private fun MicButton(isListening: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isListening) BrandYellowLighter else BrandYellow)        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = "Speak",
                modifier = Modifier.size(36.dp),
                tint = if (isListening) BrandYellow else BrandBlack
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            if (isListening) "Listening..." else "Tap to speak",
            color = GrayText
        )
    }
}